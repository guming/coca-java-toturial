package coca.bulkhead;

import coca.bulkhead.core.EventConsumer;
import coca.bulkhead.core.EventPublisher;
import coca.bulkhead.event.BulkheadEvent;
import coca.bulkhead.event.BulkheadOnCallFinishedEvent;
import coca.bulkhead.event.BulkheadOnCallPermittedEvent;
import coca.bulkhead.event.BulkheadOnCallRejectedEvent;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ThreadPoolBulkhead extends AutoCloseable {
    Metrics getMetrics();

    String getName();

    ThreadPoolBulkheadConfig getBulkheadConfig();

    Map<String, String> getTags();

    ThreadPoolBulkheadEventPublisher getEventPublisher();

    <T> CompletionStage<T> submit(Callable<T> task);

    CompletionStage<Void> submit(Runnable task);

    static <T> Supplier<CompletionStage<T>> decorateCallable(ThreadPoolBulkhead threadPoolBulkhead,
                                                             Callable<T> callable){
        return () -> threadPoolBulkhead.submit(callable);
    }
    static <T> Supplier<CompletionStage<T>> decorateSupplier(ThreadPoolBulkhead bulkhead,
                                                             Supplier<T> supplier) {
        return () -> bulkhead.submit(supplier::get);
    }

    static Supplier<CompletionStage<Void>> decorateRunnable(ThreadPoolBulkhead bulkhead, Runnable runnable) {
        return () -> bulkhead.submit(runnable);
    }

    default <T> Supplier<CompletionStage<T>> decorateSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier);
    }

    default <T> Supplier<CompletionStage<T>> decorateCallable(Callable<T> callable) {
        return decorateCallable(this, callable);
    }

    default Supplier<CompletionStage<Void>> decorateRunnable(Runnable runnable) {
        return decorateRunnable(this, runnable);
    }

    default <T> CompletionStage<T> executeSupplier(Supplier<T> supplier) {
        return decorateSupplier(this, supplier).get();
    }

    default <T> CompletionStage<T> executeCallable(Callable<T> callable) {
        return decorateCallable(this, callable).get();
    }

    default CompletionStage<Void> executeRunnable(Runnable runnable) {
        return decorateRunnable(this, runnable).get();
    }

    interface Metrics {
        int getCoreThreadPoolSize();

        int getThreadPoolSize();

        int getMaxThreadPoolSize();

        int getQueueDepth();

        int getRemainingQueueCapacity();

        int getQueueCapacity();

        int getActiveThreadCount();

        int getAvailableThreadCount();
    }

    interface ThreadPoolBulkheadEventPublisher extends EventPublisher<BulkheadEvent> {
        ThreadPoolBulkheadEventPublisher onCallRejected(
                EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer);

        ThreadPoolBulkheadEventPublisher onCallPermitted(
                EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer);

        ThreadPoolBulkheadEventPublisher onCallFinished(
                EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer);
    }
}

