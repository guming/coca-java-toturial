package coca.bulkhead;

import coca.core.EventConsumer;
import coca.core.funcs.CheckedSupplier;
import coca.bulkhead.event.BulkheadEvent;
import coca.bulkhead.event.BulkheadOnCallFinishedEvent;
import coca.bulkhead.event.BulkheadOnCallPermittedEvent;
import coca.bulkhead.event.BulkheadOnCallRejectedEvent;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface Bulkhead {
    boolean tryAcquirePermission();
    void acquirePermission();

    void releasePermission();

    void onComplete();

    String getName();

    Map<String, String> getTags();

    EventPublisher getEventPublisher();

    static <T> CheckedSupplier<T> decorateCheckedSupplier(Bulkhead bulkhead,
                                                          CheckedSupplier<T> supplier) {
        return () -> {
            bulkhead.acquirePermission();
            try {
                return supplier.get();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> Supplier<T> decorateSupplier(Bulkhead bulkhead, Supplier<T> supplier){
        return () -> {
            bulkhead.acquirePermission();
            try {
                return supplier.get();
            }finally {
                bulkhead.onComplete();
            }
        };
    }

    static Runnable decorateRunnale(Bulkhead bulkhead, Runnable runnable){
        return ()->{
            bulkhead.acquirePermission();
            try{
                runnable.run();
            }finally {
                bulkhead.onComplete();
            }
        };
    }

    static <T> Callable<T> decorateCallable(Bulkhead bulkhead, Callable<T> callable){
        return () -> {
            bulkhead.acquirePermission();
            try {
                return callable.call();
            } finally {
                bulkhead.onComplete();
            }
        };
    }

    default <T> T executeSupplier(Supplier<T> supplier){
        return decorateSupplier(this, supplier).get();
    }

    default <T> T executeCheckedSupplier(CheckedSupplier<T> supplier) throws Throwable{
        return decorateCheckedSupplier(this, supplier).get();
    }

    default void executeRunnale(Runnable runnable){
        decorateRunnale(this, runnable).run();
    }

    default <T> T executeCallable(Callable<T> callable) throws Exception {
        return decorateCallable(this, callable).call();
    }

    Metrics getMetrics();

    BulkheadConfig getBulkheadConfig();

    interface Metrics {
        int getAvailableConcurrentCalls();

        int getMaxAllowedConcurrentCalls();
    }

    interface EventPublisher extends coca.core.EventPublisher<BulkheadEvent> {
        EventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer);
        EventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer);
        EventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer);
    }

}
