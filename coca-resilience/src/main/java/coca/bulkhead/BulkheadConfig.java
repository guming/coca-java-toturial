package coca.bulkhead;

import java.io.Serializable;
import java.time.Duration;

public final class BulkheadConfig implements Serializable {
    public static final int DEFAULT_MAX_CONCURRENT_CALLS = 25;
    public static final Duration DEFAULT_MAX_WAIT_DURATION = Duration.ofSeconds(0);
    public static final boolean DEFAULT_FAIR_CALL_HANDLING_STRATEGY_ENABLED = false;
    private final int maxConcurrentCalls;
    private final Duration maxWaitDuration ;
    private final boolean fairCallHandlingEnabled;

    public BulkheadConfig.Builder custom(){
        return new BulkheadConfig.Builder();
    }

    public static BulkheadConfig ofDefaults(){
        return new Builder().build();
    }

    public BulkheadConfig(int maxConcurrentCalls, Duration maxWaitDuration,boolean fairCallHandlingEnabled) {
        this.maxConcurrentCalls = maxConcurrentCalls;
        this.maxWaitDuration = maxWaitDuration;
        this.fairCallHandlingEnabled = fairCallHandlingEnabled;
    }

    public static class Builder {

        private Duration maxWaitDuration;
        private int maxConcurrentCalls;
        private boolean fairCallHandlingEnabled;

        public Builder() {
            this.maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;
            this.maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;
            this.fairCallHandlingEnabled = DEFAULT_FAIR_CALL_HANDLING_STRATEGY_ENABLED;
        }

        public Builder(BulkheadConfig config) {
            this.maxWaitDuration = config.maxWaitDuration;
            this.maxConcurrentCalls = config.maxConcurrentCalls;
            this.fairCallHandlingEnabled = config.isFairCallHandlingEnabled();
        }

        public final Builder maxConcurrentCalls(int maxConcurrentCalls){
            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        public final Builder maxWaitDuration(Duration maxWaitDuration){
            this.maxWaitDuration = maxWaitDuration;
            return this;
        }

        public Builder fairCallHandlingStrategyEnabled(boolean fairCallHandlingEnabled) {
            this.fairCallHandlingEnabled = fairCallHandlingEnabled;
            return this;
        }

        public BulkheadConfig build(){
            return new BulkheadConfig(maxConcurrentCalls,maxWaitDuration,fairCallHandlingEnabled);
        }
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public Duration getMaxWaitDuration() {
        return maxWaitDuration;
    }

    public boolean isFairCallHandlingEnabled() {
        return fairCallHandlingEnabled;
    }
}
