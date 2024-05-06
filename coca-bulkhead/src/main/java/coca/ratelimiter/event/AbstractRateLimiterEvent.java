package coca.ratelimiter.event;

import java.time.ZonedDateTime;

public abstract class AbstractRateLimiterEvent implements RateLimiterEvent {
    private final String rateLimiterName;
    public final int numberOfPermits;
    private final ZonedDateTime creationTime;

    public AbstractRateLimiterEvent(String rateLimiterName) {
        this(1, rateLimiterName);
    }

    public AbstractRateLimiterEvent(int numberOfPermits, String rateLimiterName) {
        this.numberOfPermits = numberOfPermits;
        this.rateLimiterName = rateLimiterName;
        creationTime = ZonedDateTime.now();
    }

    @Override
    public String getRateLimiterName() {
        return rateLimiterName;
    }

    @Override
    public int getNumberOfPermits() {
        return numberOfPermits;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return "RateLimiterEvent{" +
                "type=" + getEventType() +
                ", rateLimiterName='" + getRateLimiterName() + '\'' +
                ", creationTime=" + getCreationTime() +
                '}';
    }
}
