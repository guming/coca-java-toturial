package coca.ratelimiter.event;

public class RateLimiterOnFailureEvent extends AbstractRateLimiterEvent {

    public RateLimiterOnFailureEvent(String rateLimiterName) {
        super(1, rateLimiterName);
    }

    public RateLimiterOnFailureEvent(int numberOfPermits, String rateLimiterName) {
        super(numberOfPermits, rateLimiterName);
    }

    @Override
    public Type getEventType() {
        return Type.FAILED_ACQUIRE;
    }
}
