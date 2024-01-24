package coca.concurrency.limits.internal;

import coca.concurrency.limits.MetricRegistry;

import java.util.function.Supplier;

public class EmptyMetricRegistry implements MetricRegistry {
    public static final EmptyMetricRegistry INSTANCE = new EmptyMetricRegistry();
    private EmptyMetricRegistry(){
    }
    @Override
    public SampleListener distribution(String id, String... tagNameValuePairs) {
        return value -> { };
    }

    @Override
    public void gauge(String id, Supplier<Number> supplier, String... tagNameValuePairs) {
    }

}
