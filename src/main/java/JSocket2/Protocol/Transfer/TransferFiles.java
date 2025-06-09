package JSocket2.Protocol.Transfer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class TransferFiles {
    private final RandomAccessFile file;
    private final String infoFilePath;
    private final String tmpFilePath;
    private TransferInfo info;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public TransferFiles(String tmpFilePath,String infoFilePath,String destinationPath,String fileId,String fileName,String fileExtension,int totalChunksCount,long fileSize) throws IOException {
        File tmpFile = new File(tmpFilePath);
        File parentDir = tmpFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        this.tmpFilePath = tmpFilePath;
        this.file = new RandomAccessFile(tmpFilePath,"rw");
        this.infoFilePath = infoFilePath;
        File mf = new File(infoFilePath);
        if (mf.exists()) {
            readInfoFromDisk(mf);
        } else {
            this.info = new TransferInfo(fileId,fileName,fileExtension,destinationPath, 0, 0,totalChunksCount,fileSize);
            saveinfoToDisk();
        }
    }

    public static boolean canLoad(String fileId) {
        String systemTempDir = System.getProperty("java.io.tmpdir");
        String basePath = Paths.get(systemTempDir, "JTelegram").toString();

        File tmpFile = new File(basePath, fileId + ".tmp");
        File infoFile = new File(basePath, fileId + ".info");

        return tmpFile.exists() && infoFile.exists();
    }


    public static TransferFiles Load(String fileId) {
        String systemTempDir = System.getProperty("java.io.tmpdir");
        String basePath = Paths.get(systemTempDir, "JTelegram").toString();

        File infoFile = new File(basePath, fileId + ".info");
        File tmpFile = new File(basePath, fileId + ".tmp");

        if (!tmpFile.exists() || !infoFile.exists()) {
            return null;
        }

        try {
            Gson gson = new Gson();
            TransferInfo info;
            try (FileReader reader = new FileReader(infoFile)) {
                info = gson.fromJson(reader, TransferInfo.class);
            }

            return new TransferFiles(
                    tmpFile.getAbsolutePath(),
                    infoFile.getAbsolutePath(),
                    info.getDestinationPath(),
                    fileId,
                    info.getFileName(),
                    info.getFileExtension(),
                    info.getTotalChunksCount(),
                    info.getFileSize()
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized void writeChunk(long offset, byte[] data, int chunkIndex) throws IOException {

        file.seek(offset);
        file.write(data);

        info.setLastWrittenOffset(offset + data.length);
        info.setLastChunkIndex(chunkIndex);

        saveinfoToDisk();
    }

    public synchronized void closeAndCleanup() throws IOException {
        if (file != null) {
            file.close();
        }
        if(info.getTotalChunksCount()-1 == info.getLastChunkIndex()) {
            info.setTransferState(TransferState.Complete);
        }else{
            info.setTransferState(TransferState.Paused);
        }
        saveinfoToDisk();
    }
    public String getTmpFilePath(){
        return tmpFilePath;
    }
    public String getinfoFilePath() {
        return infoFilePath;
    }

    public synchronized TransferInfo getinfo() throws IOException {
        if(info == null){
            readInfoFromDisk();
        }
        return info;
    }
    public void finalizeTransfer() throws IOException {
        closeAndCleanup();

        File destinationDir = new File(info.getDestinationPath());
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        String baseName = info.getFileName();
        String extension = info.getFileExtension();
        File finalFile = new File(destinationDir, baseName + "." + extension);
        int index = 1;
        while (finalFile.exists()) {
            finalFile = new File(destinationDir, baseName + " (" + index + ")." + extension);
            index++;
        }

        Files.move(Path.of(tmpFilePath), finalFile.toPath());
    }
    private void saveinfoToDisk() throws IOException {
        try (FileWriter writer = new FileWriter(infoFilePath)) {
            gson.toJson(info, writer);
        }
    }
    private void readInfoFromDisk() throws IOException {
        readInfoFromDisk(new File(infoFilePath));
    }
    private void readInfoFromDisk(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            this.info = gson.fromJson(reader, TransferInfo.class);
        }
    }
    public static TransferFiles Create(String fileId,String fileName,String fileExtension,String destinationPath, String tempDirectoryPath,int totalChunks,long fileSize) throws IOException {
        String tmpFileName = fileId + ".tmp";
        String infoFileName = fileId + ".info";
        String tmpfilePath = Paths.get(tempDirectoryPath, tmpFileName).toString();
        String infoFilePath = Paths.get(tempDirectoryPath, infoFileName).toString();
        return new TransferFiles(tmpfilePath,infoFilePath,destinationPath,fileId,fileName,fileExtension,totalChunks,fileSize);
    }
    public static TransferFiles CreateDefault(String fileId,String fileName,String fileExtension,String destinationPath,int totalChunks,long fileSize) throws IOException {
        String systemTempDir = System.getProperty("java.io.tmpdir");
        return Create(fileId,fileName,fileExtension,destinationPath,Paths.get(systemTempDir,"JTelegram").toString(),totalChunks,fileSize);
    }

    public boolean isComplete() {
        return info.getLastChunkIndex() == info.getTotalChunksCount()-1;
    }
}
