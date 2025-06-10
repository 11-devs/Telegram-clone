package JSocket.Protocol.Transfer;

public class SendChunkMetadata {
    public String fileId;
    public int chunkIndex;
    public int totalChunks;
    public long offset;
}
