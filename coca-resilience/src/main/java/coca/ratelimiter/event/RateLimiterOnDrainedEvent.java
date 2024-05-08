package coca.ratelimiter.event;

public class RateLimiterOnDrainedEvent extends AbstractRateLimiterEvent {
    public RateLimiterOnDrainedEvent(String rateLimiterName) {
        super(1, rateLimiterName);
    }

    public RateLimiterOnDrainedEvent(int numberOfPermits, String rateLimiterName) {
        super(numberOfPermits, rateLimiterName);
    }

    @Override
    public Type getEventType() {
        return Type.DRAINED;
    }

}
