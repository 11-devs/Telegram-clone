package JSocket2.Core.Client;

public class ConnectionManagerOptions {
    private int minRetryDelay = 3000;
    private int maxRetryDelay = 15000;
    private int maxTryCount_for_changeRetryDelay = 5;
    private float coefficient_jitter = 0.5f;

    public int getMinRetryDelay() {
        return minRetryDelay;
    }

    public void setMinRetryDelay(int minRetryDelay) {
        this.minRetryDelay = minRetryDelay;
    }

    public float getCoefficient_jitter() {
        return coefficient_jitter;
    }

    public int getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public int getMaxTryCount_for_changeRetryDelay() {
        return maxTryCount_for_changeRetryDelay;
    }

    public void setCoefficient_jitter(float coefficient_jitter) throws Exception {
        if(coefficient_jitter > 1) throw new Exception("coefficient_jitter must be less than 1");
        this.coefficient_jitter = coefficient_jitter;
    }

    public void setMaxRetryDelay(int maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    public void setMaxTryCount_for_changeRetryDelay(int maxTryCount_for_changeRetryDelay) {
        this.maxTryCount_for_changeRetryDelay = maxTryCount_for_changeRetryDelay;
    }
}
