package Client.Services;

import Client.AppConnectionManager;
import Client.Tasks.DownloadTask;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Transfer.*;
import JSocket2.Protocol.Transfer.Download.DownloadFileInfoModel;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * A singleton service to manage file downloads, caching, and progress.
 */
public class FileDownloadService {
    private static FileDownloadService instance;
    private ConnectionManager connectionManager;
    private ClientFileTransferManager transferManager;
    private ExecutorService executor;
    private final Path cacheDir = Paths.get("ClientData/cache/avatars");
    private final Path documentCacheDir = Paths.get("ClientData/cache/documents");
    private final Map<String, CompletableFuture<File>> activeDownloads = new ConcurrentHashMap<>();
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private FileDownloadService() {
        try {
            Files.createDirectories(cacheDir);
            Files.createDirectories(documentCacheDir);
        } catch (IOException e) {
            System.err.println("Failed to create cache directories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the singleton instance of the service.
     *
     * @return The FileDownloadService instance.
     */
    public static synchronized FileDownloadService getInstance() {
        if (instance == null) {
            instance = new FileDownloadService();
        }
        return instance;
    }

    /**
     * Initializes the service with the necessary connection manager.
     * This should be called once when the application starts.
     */
    public void initialize() {
        this.connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        this.transferManager = connectionManager.getClient().getFileTransferManager();
        this.executor = connectionManager.getClient().getBackgroundExecutor();
    }
    public Path getDocumentCacheDir() {
        return documentCacheDir;
    }

    /**
     * Retrieves a file, downloading it if it's not already cached.
     *
     * @param fileId The unique ID of the file to retrieve.
     * @return A CompletableFuture that will complete with the File object once it's available locally.
     */
    public CompletableFuture<File> getFile(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("File ID cannot be null or empty."));
        }

        // If a download for this file is already in progress, return its future.
        return activeDownloads.computeIfAbsent(fileId, id -> {
            CompletableFuture<File> future = new CompletableFuture<>();
            executor.submit(() -> downloadAndCacheFile(id, future));
            return future;
        });
    }
    private final Map<String, TransferInfo> infoCache = new ConcurrentHashMap<>();

    //... existing methods
    public CompletableFuture<Image> getImage(String fileId) {
        if (imageCache.containsKey(fileId)) {
            return CompletableFuture.completedFuture(imageCache.get(fileId));
        }

        // getFile handles the file-level caching and downloading
        return getFile(fileId).thenApplyAsync(file -> {
            try {
                // The Image constructor with background loading is efficient.
                Image image = new Image(file.toURI().toString(), true);
                if (!image.isError()) {
                    // Cache the successfully loaded image for future requests.
                    imageCache.put(fileId, image);
                }
                return image;
            } catch (Exception e) {
                System.err.println("Failed to create Image object for fileId " + fileId + ": " + e.getMessage());
                return null; // The caller should handle the null case.
            }
        }, executor); // Use the background executor
    }
    /**
     * Asynchronously retrieves metadata for a file from the server.
     * This method initiates a download to get the TransferInfo but does not download the file chunks.
     * It's used to get file details (name, size) for rendering UI elements.
     *
     * @param fileId The unique ID of the file.
     * @return A CompletableFuture that will complete with the file's TransferInfo.
     */
    public CompletableFuture<TransferInfo> getFileInfo(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            System.err.println("FileDownloadService.getFileInfo was called with a null or blank fileId.");
            return CompletableFuture.failedFuture(new IllegalArgumentException("File ID cannot be null or blank."));
        }
        if (infoCache.containsKey(fileId)) {
            return CompletableFuture.completedFuture(infoCache.get(fileId));
        }

        CompletableFuture<TransferInfo> future = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                if (TransferFiles.canLoad(fileId)) {
                    try {
                        TransferFiles tf = TransferFiles.Load(fileId);
                        if (tf != null) {
                            TransferInfo localInfo = tf.getinfo();
                            infoCache.put(fileId, localInfo);
                            future.complete(localInfo);
                            return; // Found local info, we are done.
                        }
                    } catch (Exception e) {
                        System.err.println("Could not load existing transfer info for " + fileId + ". Fetching from server. " + e.getMessage());
                    }
                }

                // If not resumable, fetch info from server without creating local files.
                DownloadFileInfoModel fileInfoModel = transferManager.getDownloadFileInfoFromServer(fileId);

                if (fileInfoModel != null) {
                    // Manually construct a transient TransferInfo object.
                    int totalChunksCount = (int) Math.ceil((double) fileInfoModel.getFileLength() / 65536); // Assuming default chunk size 64KB
                    TransferInfo info = new TransferInfo(
                            fileInfoModel.getFileId(),
                            fileInfoModel.getFileName(),
                            fileInfoModel.getFileExtension(),
                            cacheDir.toString(), // The potential destination path
                            0,               // lastWrittenOffset, not started yet
                            0,               // lastChunkIndex, not started yet
                            totalChunksCount,
                            fileInfoModel.getFileLength()
                    );
                    info.setTransferState(TransferState.Paused); // Indicate it's just info, not in progress.

                    infoCache.put(fileId, info);
                    future.complete(info);
                } else {
                    future.completeExceptionally(new IOException("Failed to retrieve file info from server."));
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    private void downloadAndCacheFile(String fileId, CompletableFuture<File> future) {
        try {
            TransferInfo info = transferManager.initiateDownload(fileId, cacheDir.toString());
            if (info == null) {
                throw new IOException("Failed to initiate download for " + fileId + ". TransferInfo is null.");
            }

            File finalFile = new File(info.getDestinationPath(), info.getFileName() + "." + info.getFileExtension());

            // If the file already exists and the transfer is marked as complete, use it.
            if (finalFile.exists() && info.getTransferState() == TransferState.Complete) {
                Platform.runLater(() -> future.complete(finalFile));
                activeDownloads.remove(fileId);
                return;
            }

            //File tmpFile = new File(info.getTmpFilePath());
            IProgressListener listener = (transferred, total) -> {
                double progress = (total > 0) ? ((double) transferred / total) * 100 : 0;
                System.out.printf("Download Progress for %s: %.2f%%%n", fileId, progress);
            };

            DownloadTask downloadTask = new DownloadTask(transferManager, info, null, fileId, listener);

            downloadTask.setOnSucceeded(e -> {
                Platform.runLater(() -> future.complete(finalFile));
                activeDownloads.remove(fileId);
            });

            downloadTask.setOnFailed(e -> {
                Platform.runLater(() -> future.completeExceptionally(downloadTask.getException()));
                activeDownloads.remove(fileId);
            });

            executor.submit(downloadTask);

        } catch (Exception e) {
            Platform.runLater(() -> future.completeExceptionally(e));
            activeDownloads.remove(fileId);
        }
    }
}