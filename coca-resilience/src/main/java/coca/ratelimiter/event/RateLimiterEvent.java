package coca.ratelimiter.event;

import java.time.ZonedDateTime;

public interface RateLimiterEvent {
    String getRateLimiterName();

    Type getEventType();

    int getNumberOfPermits();

    ZonedDateTime getCreationTime();

    enum Type {
        FAILED_ACQUIRE,
        SUCCESSFUL_ACQUIRE,
        DRAINED
    }
}
