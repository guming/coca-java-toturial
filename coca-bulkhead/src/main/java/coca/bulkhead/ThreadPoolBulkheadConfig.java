package coca.bulkhead;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolBulkheadConfig {
    public static final int DEFAULT_QUEUE_CAPACITY = 100;
    public static final Duration DEFAULT_KEEP_ALIVE_DURATION = Duration.ofMillis(20);
    public static final int DEFAULT_CORE_THREAD_POOL_SIZE =
            Runtime.getRuntime().availableProcessors() > 1 ? Runtime.getRuntime().availableProcessors()
                    - 1 : 1;
    public static final int DEFAULT_MAX_THREAD_POOL_SIZE = Runtime.getRuntime()
            .availableProcessors();
    private int maxThreadPoolSize = DEFAULT_MAX_THREAD_POOL_SIZE;
    private int coreThreadPoolSize = DEFAULT_CORE_THREAD_POOL_SIZE;
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
    private Duration keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;

    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public int getCoreThreadPoolSize() {
        return coreThreadPoolSize;
    }


    public int getQueueCapacity() {
        return queueCapacity;
    }

    public Duration getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler(){
        return rejectedExecutionHandler;
    }

    private ThreadPoolBulkheadConfig(){}

    public static Builder custom(){
        return new Builder();
    }

    public static ThreadPoolBulkheadConfig ofDefaults() {
        return new Builder().build();
    }

    public static class Builder {
        private ThreadPoolBulkheadConfig config;

        public Builder() {
            this.config = new ThreadPoolBulkheadConfig();
        }
        public Builder maxThreadPoolSize(int maxThreadPoolSize){
            if (maxThreadPoolSize < 1) {
                throw new IllegalArgumentException(
                        "maxThreadPoolSize must be a positive integer value >= 1");
            }
            config.maxThreadPoolSize = maxThreadPoolSize;
            return this;
        }

        public Builder coreThreadPoolSize(int coreThreadPoolSize){
            if(coreThreadPoolSize<1){
                throw new IllegalStateException("coreThreadPoolSize must be a positive integer value >=1");
            }
            config.coreThreadPoolSize = coreThreadPoolSize;
            return this;
        }

        public Builder queueCapacity(int queueCapacity) {
            if (queueCapacity < 0) {
                throw new IllegalArgumentException(
                        "queueCapacity must be a positive integer value >= 0");
            }
            config.queueCapacity = queueCapacity;
            return this;
        }

        public Builder keepAliveDuration(Duration keepAliveDuration) {
            if (keepAliveDuration.toMillis() < 0) {
                throw new IllegalArgumentException(
                        "keepAliveDuration must be a positive integer value >= 0");
            }
            config.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Builder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            Objects.requireNonNull(rejectedExecutionHandler);
            config.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        public ThreadPoolBulkheadConfig build(){
            return config;
        }

    }
}
