package coca.concurrency.limits.limit.window;

public class ImmutableAverageSampleWindow implements SampleWindow {
    private final long minRtt;
    private final int maxInFlight;
    private final long sumRtt;
    private final boolean didDrop;
    private final int sampleCount;

    public ImmutableAverageSampleWindow() {
        this.minRtt = Long.MAX_VALUE;
        this.maxInFlight = 0;
        this.sumRtt = 0;
        this.didDrop = false;
        this.sampleCount = 0;
    }
    public ImmutableAverageSampleWindow(long minRtt, int maxInFlight, long sum, boolean didDrop, int sampleCount) {
        this.minRtt = minRtt;
        this.maxInFlight = maxInFlight;
        this.sumRtt = sum;
        this.didDrop = didDrop;
        this.sampleCount = sampleCount;
    }


    @Override
    public ImmutableAverageSampleWindow addSample(long rtt, int inflight, boolean dropped) {
        return new ImmutableAverageSampleWindow(Math.min(rtt, minRtt), Math.max(maxInFlight, inflight), sumRtt + rtt, didDrop||dropped, sampleCount + 1);
    }

    @Override
    public boolean didDrop() {
        return didDrop;
    }

    @Override
    public int getMaxInFlight() {
        return maxInFlight;
    }

    @Override
    public int getSampleCount() {
        return sampleCount;
    }

    @Override
    public long getMinRtt() {
        return minRtt;
    }

    @Override
    public long getTrackedRttNanos() {
        return sampleCount == 0 ? 0 : sumRtt / sampleCount;
    }
}
