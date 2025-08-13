package JSocket2.Protocol.Transfer;

public interface IProgressListener {
    void onProgress(long transferred, long total);
}