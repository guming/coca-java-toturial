package coca.concurrency.limits.limit;

public class FixedLimit extends AbstractLimit {
    @Override
    protected int _update(long startTime, long rtt, int inflight, boolean didDrop) {
        return getLimit();
    }
    private FixedLimit(int limit) {
        super(limit);
    }
    public static FixedLimit of(int limit){
        return new FixedLimit(limit);
    }
}
