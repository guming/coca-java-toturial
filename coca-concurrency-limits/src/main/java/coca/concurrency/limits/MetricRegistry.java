package coca.concurrency.limits;

import java.util.function.Supplier;

public interface MetricRegistry {
    interface SampleListener{
        void addSamole(Number value);
    }
    interface Counter {
        void increment();
    }
    default Counter counter(String id, String... tagNameValuePairs) {
        return () -> {};
    }
    default void gauge(String id, Supplier<Number> supplier, String... tagNameValuePairs) {
        registerGauge(id, supplier, tagNameValuePairs);
    };
    default void registerGauge(String id, Supplier<Number> supplier, String... tagNameValuePairs) {
        throw new UnsupportedOperationException("registerDistribution is deprecated");
    }
    @Deprecated
    default void registerGuage(String id, Supplier<Number> supplier, String... tagNameValuePairs) {
        gauge(id, supplier, tagNameValuePairs);
    }
    default SampleListener distribution(String id, String... tagNameValuePairs) {
        return registerDistribution(id, tagNameValuePairs);
    }
    @Deprecated
    default SampleListener registerDistribution(String id, String... tagNameValuePairs) {
        throw new UnsupportedOperationException("registerDistribution is deprecated");
    }
}
