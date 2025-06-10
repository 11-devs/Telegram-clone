package JSocket.Protocol.Transfer;

public class FileInfoModel {
    public String FileId;
    public int ChunkSize = 65536;
    public FileInfoModel(String FileId,int ChunkSize){
        this.FileId = FileId;
        this.ChunkSize = ChunkSize;
    }
    public FileInfoModel(String FileId){
        this.FileId = FileId;
    }
}
