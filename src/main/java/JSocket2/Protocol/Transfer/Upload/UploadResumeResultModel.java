package JSocket2.Protocol.Transfer.Upload;

public class UploadResumeResultModel {
    public final String FileId;
    public final int StartIndex;
    public final long StartOffset;
    public final int ChunkSize;
    public final long FileSize;
    public UploadResumeResultModel(String FileId, int StartIndex, long StartOffset, int ChunkSize, long FileSize){
        this.FileId = FileId;
        this.StartIndex = StartIndex;
        this.StartOffset = StartOffset;
        this.ChunkSize = ChunkSize;
        this.FileSize = FileSize;
    }
}
