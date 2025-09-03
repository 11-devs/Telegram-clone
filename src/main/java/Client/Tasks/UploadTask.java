package Client.Tasks;

import JSocket2.Protocol.Transfer.ClientFileTransferManager;
import JSocket2.Protocol.Transfer.FileInfoModel;
import JSocket2.Protocol.Transfer.IProgressListener;
import javafx.concurrent.Task;

import java.io.File;

public class UploadTask extends Task<Void> {
    private final ClientFileTransferManager clientFileTransferManager;
    private final FileInfoModel info;
    private final File file;
    private final IProgressListener progressListener;
    public UploadTask(ClientFileTransferManager clientFileTransferManager, FileInfoModel info, File file, IProgressListener progressListener) {
        this.clientFileTransferManager = clientFileTransferManager;
        this.info = info;
        this.file = file;
        this.progressListener = progressListener;
    }

    @Override
    protected Void call() throws Exception {
        long totalBytes = file.length();
        clientFileTransferManager.registerTransferListener(info.FileId,progressListener,(()->!isCancelled()));
        clientFileTransferManager.StartUpload(info, file);
        //updateProgress(totalBytes, totalBytes);
        return null;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }
}

