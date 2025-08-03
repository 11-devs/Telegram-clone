package JSocket2.Example;

import JSocket2.Protocol.Transfer.IProgressListener;

public class ProcessListener implements IProgressListener {
    @Override
    public void onProgress(long transferred, long total) {
        int width = 50;
        double ratio = (double) transferred / total;
        int filled = (int) (ratio * width);
        int percent = (int) (ratio * 100);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            bar.append(i < filled ? "=" : " ");
        }
        bar.append("] ").append(percent).append("%");

        System.out.print("\r" + bar);
    }
}
