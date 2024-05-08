package coca.ratelimiter;

import coca.core.EventConsumer;
import coca.core.exception.AcquirePermissionCancelledException;
import coca.ratelimiter.event.RateLimiterEvent;
import coca.ratelimiter.event.RateLimiterOnFailureEvent;
import coca.ratelimiter.event.RateLimiterOnSuccessEvent;

import java.time.Duration;
import java.util.Map;

public interface RateLimiter {
    String getName();
    Map<String, String> getTags();
    EventPublisher getEventPublisher();
    RateLimiterConfig getRateLimiterConfig();
    default boolean acquirePermission() {
        return acquirePermission(1);
    }
    boolean acquirePermission(int permits);
    default long reservePermission() {
        return reservePermission(1);
    }
    long reservePermission(int permits);
    void drainPermissions();
    void changeTimeoutDuration(Duration timeoutDuration);
    void changeLimitForPeriod(int limitForPeriod);

    static void waitForPermission(final RateLimiter rateLimiter, int permits) {
        boolean permission = rateLimiter.acquirePermission(permits);
        if (Thread.currentThread().isInterrupted()) {
            throw new AcquirePermissionCancelledException();
        }
        if (!permission) {
            throw RequestNotPermitted.createRequestNotPermitted(rateLimiter);
        }
    }

    interface EventPublisher extends coca.core.EventPublisher<RateLimiterEvent> {
        EventPublisher onSuccess(EventConsumer<RateLimiterOnSuccessEvent> consumer);
        EventPublisher onFailure(EventConsumer<RateLimiterOnFailureEvent> consumer);
    }
}
