package Client.Tasks;

import JSocket2.Protocol.Transfer.*;
import javafx.concurrent.Task;

import java.io.File;

public class DownloadTask extends Task<Void> {
    private final ClientFileTransferManager clientFileTransferManager;
    private final TransferInfo info;
    private final File destination;
    private final String FileId;
    private final IProgressListener progressListener;

    public DownloadTask(ClientFileTransferManager clientFileTransferManager,
                        TransferInfo info,
                        File destination, String fileId, IProgressListener progressListener) {
        this.clientFileTransferManager = clientFileTransferManager;
        this.info = info;
        this.destination = destination;
        FileId = fileId;
        this.progressListener = progressListener;
    }

    @Override
    protected Void call() throws Exception {
        clientFileTransferManager.registerTransferListener(FileId,progressListener,(()->!isCancelled()));
        clientFileTransferManager.sendDownloadChunkRequest(info.getFileId(),info.getLastChunkIndex(),info.getLastWrittenOffset());
        while (!isCancelled()) {
            TransferFiles t = clientFileTransferManager.getActiveTransfers().get(info.getFileId());
            if (t == null || t.isComplete()) {
                break;
            }
            Thread.sleep(200);
        }

        return null;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }
}
