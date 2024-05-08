package coca.ratelimiter;

import coca.core.EventConsumer;
import coca.core.exception.AcquirePermissionCancelledException;
import coca.core.funcs.CheckedSupplier;
import coca.ratelimiter.event.RateLimiterEvent;
import coca.ratelimiter.event.RateLimiterOnFailureEvent;
import coca.ratelimiter.event.RateLimiterOnSuccessEvent;
import coca.ratelimiter.internal.AtomicRateLimiter;
import io.vavr.control.Either;

import java.time.Duration;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public interface RateLimiter {
    String getName();
    Map<String, String> getTags();
    EventPublisher getEventPublisher();
    RateLimiterConfig getRateLimiterConfig();

    static RateLimiter of(String name, RateLimiterConfig rateLimiterConfig) {
        return of(name, rateLimiterConfig, emptyMap());
    }

    static RateLimiter of(String name, RateLimiterConfig rateLimiterConfig,
                          Map<String, String> tags) {
        return new AtomicRateLimiter(tags, name, rateLimiterConfig);
    }

    default <T> T executeSupplier(Supplier<T> supplier) {
        return executeSupplier(1, supplier);
    }
    default <T> T executeSupplier(int permits, Supplier<T> supplier) {
        return decorateSupplier(this, permits, supplier).get();
    }

    static <T> Supplier<T> decorateSupplier(RateLimiter rateLimiter, int permits, Supplier<T> supplier) {
        return decorateCheckedSupplier(rateLimiter, permits, supplier::get)
                .unchecked();
    }
    static <T> CheckedSupplier<T> decorateCheckedSupplier(RateLimiter rateLimiter, int permits,
                                                          CheckedSupplier<T> supplier) {
        return ()-> {
            waitForPermission(rateLimiter,permits);
            try{
                T result = supplier.get();
                rateLimiter.onResult(result);
                return result;
            }catch (Exception e) {
                rateLimiter.onError(e);
                throw e;
            }
        };
    }

    static <T> Supplier<T> decorateSupplier(RateLimiter rateLimiter, Supplier<T> supplier) {
        return decorateSupplier(rateLimiter, 1, supplier);
    }


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
    default void onError(Throwable throwable) {
        drainIfNeeded(Either.left(throwable));
    }
    default void onSuccess() {
        drainIfNeeded(Either.right(null));
    }

    default void onResult(Object result) {
        drainIfNeeded(Either.right(result));
    }
    default void drainIfNeeded(Either<? extends Throwable, ?> callsResult) {
        Predicate<Either<? extends Throwable, ?>> checker = getRateLimiterConfig()
                .getDrainPermissionsOnResult();
        if (checker != null && checker.test(callsResult)) {
            drainPermissions();
        }
    }


    interface EventPublisher extends coca.core.EventPublisher<RateLimiterEvent> {
        EventPublisher onSuccess(EventConsumer<RateLimiterOnSuccessEvent> consumer);
        EventPublisher onFailure(EventConsumer<RateLimiterOnFailureEvent> consumer);
    }
}
