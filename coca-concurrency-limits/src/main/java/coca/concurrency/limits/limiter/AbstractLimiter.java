package coca.concurrency.limits.limiter;

import coca.concurrency.limits.Limit;
import coca.concurrency.limits.MetricRegistry;
import coca.concurrency.limits.MetricValues;
import coca.concurrency.limits.internal.EmptyMetricRegistry;
import coca.concurrency.limits.limit.VegasLimit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class AbstractLimiter {
    public static final String ID_TAG = "id";
    public static final String STATUS_TAG = "status";
    private final AtomicInteger inFlight = new AtomicInteger();
    private final Limit limitAlgorithm;
    private final MetricRegistry.Counter successCounter;
    private final MetricRegistry.Counter droppedCounter;
    private final MetricRegistry.Counter ignoredCounter;
    private final MetricRegistry.Counter rejectedCounter;
    private volatile int limit;
    private final Supplier<Long> clock;
    public abstract static class Builder<BuilderT extends Builder<BuilderT>> {
        private static final AtomicInteger idCounter = new AtomicInteger();

        private Limit limit = VegasLimit.newInstance();
        private Supplier<Long> clock = System::nanoTime;

        protected String name = "unnamed-" + idCounter.incrementAndGet();
        protected MetricRegistry registry = EmptyMetricRegistry.INSTANCE;

        public BuilderT named(String name) {
            this.name = name;
            return self();
        }

        public BuilderT limit(Limit limit) {
            this.limit = limit;
            return self();
        }

        public BuilderT clock(Supplier<Long> clock) {
            this.clock = clock;
            return self();
        }

        public BuilderT metricRegistry(MetricRegistry registry) {
            this.registry = registry;
            return self();
        }

        protected abstract BuilderT self();
    }
    public AbstractLimiter(Builder<?> builder) {
        this.clock = builder.clock;
        this.limitAlgorithm = builder.limit;
        this.limit = limitAlgorithm.getLimit();
        this.limitAlgorithm.notifyOnChange(this::onNewLimit);

        builder.registry.gauge(MetricValues.LIMIT_NAME, this::getLimit);
        this.successCounter = builder.registry.counter(MetricValues.CALL_NAME, ID_TAG, builder.name, STATUS_TAG, "success");
        this.droppedCounter = builder.registry.counter(MetricValues.CALL_NAME, ID_TAG, builder.name, STATUS_TAG, "dropped");
        this.ignoredCounter = builder.registry.counter(MetricValues.CALL_NAME, ID_TAG, builder.name, STATUS_TAG, "ignored");
        this.rejectedCounter = builder.registry.counter(MetricValues.CALL_NAME, ID_TAG, builder.name, STATUS_TAG, "rejected");
    }

    public int getInFlight() {
        return inFlight.get();
    }

    public int getLimit() {
        return limit;
    }

    protected void onNewLimit(int newLimit) {
        limit = newLimit;
    }
}
