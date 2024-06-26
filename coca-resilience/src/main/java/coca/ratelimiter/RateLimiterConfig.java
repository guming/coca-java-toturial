package coca.ratelimiter;

import io.vavr.control.Either;

import java.time.Duration;
import java.util.function.Predicate;

public class RateLimiterConfig {
    private final int limitForPeriod;
    private final Duration timeoutDuration;
    private final Duration limitRefreshInterval;
    private static final Duration ACCEPTABLE_REFRESH_PERIOD = Duration.ofNanos(1L);
    private final transient Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult;
    public static RateLimiterConfig ofDefaults() {
        return new Builder().build();
    }

    public RateLimiterConfig(int limitForPeriod, Duration timeDuration, Duration limitRefreshInterval, Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult) {
        this.limitForPeriod = limitForPeriod;
        this.timeoutDuration = timeDuration;
        this.limitRefreshInterval = limitRefreshInterval;
        this.drainPermissionsOnResult = drainPermissionsOnResult;
    }

    public static Builder from(RateLimiterConfig prototype) {
        return new Builder(prototype);
    }

    public static Builder custom() {
        return new Builder();
    }

    public int getLimitForPeriod() {
        return limitForPeriod;
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public Duration getLimitRefreshInterval() {
        return limitRefreshInterval;
    }

    public static class Builder {

        private int limitForPeriod =50;
        private Duration timeoutDuration = Duration.ofSeconds(5L);
        private Duration limitRefreshInterval = Duration.ofNanos(500);
        private Predicate<Either<? extends Throwable, ?>> drainPermissionsOnResult = any -> false;

        public Builder() {
        }

        public Builder(RateLimiterConfig config) {
            this.limitForPeriod = config.limitForPeriod;
            this.timeoutDuration = config.timeoutDuration;
            this.limitRefreshInterval = config.limitRefreshInterval;
            this.drainPermissionsOnResult = config.drainPermissionsOnResult;
        }
        public Builder limitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
            return this;
        }
        public Builder timeoutDuration(Duration timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
            return this;
        }
        public Builder limitRefreshInterval(Duration limitRefreshInterval) {
            this.limitRefreshInterval = limitRefreshInterval;
            return this;
        }
        public RateLimiterConfig build() {
            return new RateLimiterConfig(limitForPeriod, timeoutDuration, limitRefreshInterval,drainPermissionsOnResult);
        }
    }
    public Predicate<Either<? extends Throwable, ?>> getDrainPermissionsOnResult() {
        return drainPermissionsOnResult;
    }
}
