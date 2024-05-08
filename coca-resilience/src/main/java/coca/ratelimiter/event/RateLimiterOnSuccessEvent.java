package coca.ratelimiter.event;

public class RateLimiterOnSuccessEvent extends AbstractRateLimiterEvent {

    public RateLimiterOnSuccessEvent(int numberOfPermits, String rateLimiterName) {
        super(numberOfPermits, rateLimiterName);
    }

    public RateLimiterOnSuccessEvent(String rateLimiterName) {
        super(1,rateLimiterName);
    }

    @Override
    public Type getEventType() {
        return Type.SUCCESSFUL_ACQUIRE;
    }
}
