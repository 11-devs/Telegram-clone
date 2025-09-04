package Client.Services;

import Client.AppConnectionManager;
import Client.Tasks.DownloadTask;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Transfer.ClientFileTransferManager;
import JSocket2.Protocol.Transfer.IProgressListener;
import JSocket2.Protocol.Transfer.TransferInfo;
import JSocket2.Protocol.Transfer.TransferState;
import javafx.application.Platform;

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
    private final Map<String, CompletableFuture<File>> activeDownloads = new ConcurrentHashMap<>();

    private FileDownloadService() {
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            System.err.println("Failed to create cache directory: " + cacheDir);
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