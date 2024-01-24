package coca.concurrency.limits.internal;

import coca.concurrency.limits.MetricRegistry;

public class EmptyMetricRegistry implements MetricRegistry {
    public static final EmptyMetricRegistry INSTANCE = new EmptyMetricRegistry();
    private EmptyMetricRegistry(){

    }

}
