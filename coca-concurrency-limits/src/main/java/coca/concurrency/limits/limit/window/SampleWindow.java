package coca.concurrency.limits.limit.window;

public interface SampleWindow {
    SampleWindow addSample(long rtt, int inflight, boolean dropped);

    boolean didDrop();

    int getMaxInFlight();

    int getSampleCount();

    long getMinRtt();

    long getTrackedRttNanos();

}
