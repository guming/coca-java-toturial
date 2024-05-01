package coca.bulkhead.internal;

import coca.bulkhead.ThreadPoolBulkhead;
import coca.bulkhead.ThreadPoolBulkheadConfig;
import coca.core.EventConsumer;
import coca.core.EventProcessor;
import coca.bulkhead.event.BulkheadEvent;
import coca.bulkhead.event.BulkheadOnCallFinishedEvent;
import coca.bulkhead.event.BulkheadOnCallPermittedEvent;
import coca.bulkhead.event.BulkheadOnCallRejectedEvent;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class FixedThreadPoolBulkhead implements ThreadPoolBulkhead {

    private final ThreadPoolExecutor executorService;
    private final ThreadPoolBulkheadConfig config;
    private final Map<String, String> tags;

    private final String name;
    private final FixedThreadPoolBulkhead.BulkheadMetrics metrics;
    private final FixedThreadPoolBulkhead.BulkheadEventProcessor eventProcessor;


    public FixedThreadPoolBulkhead(ThreadPoolBulkheadConfig config, String name, Map<String, String> tags) {
        this.config = config;
        this.name = name;
        this.tags = tags;
        this.executorService = new ThreadPoolExecutor(config.getCoreThreadPoolSize(), config.getMaxThreadPoolSize(), config.getKeepAliveDuration().toMillis(), TimeUnit.MILLISECONDS,
                config.getQueueCapacity() == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(config.getQueueCapacity()),
                config.getRejectedExecutionHandler());
        this.metrics = new FixedThreadPoolBulkhead.BulkheadMetrics();
        this.eventProcessor = new BulkheadEventProcessor();
    }
    public FixedThreadPoolBulkhead(String name, Supplier<ThreadPoolBulkheadConfig> configSupplier) {
        this(configSupplier.get(),name, emptyMap());
    }

    public FixedThreadPoolBulkhead(String name) {
        this(ThreadPoolBulkheadConfig.ofDefaults(), name, emptyMap());
    }

    public FixedThreadPoolBulkhead(String name, ThreadPoolBulkheadConfig bulkheadConfig) {
        this( bulkheadConfig, name, emptyMap());
    }

    public FixedThreadPoolBulkhead(String name, Map<String, String> tags) {
        this(ThreadPoolBulkheadConfig.ofDefaults(), name, tags);
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ThreadPoolBulkheadConfig getBulkheadConfig() {
        return config;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public ThreadPoolBulkheadEventPublisher getEventPublisher() {
        return eventProcessor;
    }

    @Override
    public <T> CompletionStage<T> submit(Callable<T> task) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        try {
            CompletableFuture.supplyAsync(()->{
                try {
                    publishBulkheadEvent(() -> new BulkheadOnCallPermittedEvent(name));
                    return task.call();
                } catch (CompletionException e) {
                    throw e;
                } catch (Exception e){
                    throw new CompletionException(e);
                }
            }, executorService).whenComplete((result, throwable) -> {
                publishBulkheadEvent(() -> new BulkheadOnCallFinishedEvent(name));
                if (throwable != null) {
                    promise.completeExceptionally(throwable);
                } else {
                    promise.complete(result);
                }
            });
        } catch (RejectedExecutionException e) {
            publishBulkheadEvent(() -> new BulkheadOnCallRejectedEvent(name));
            throw e;
        }
        return promise;
    }

    @Override
    public CompletionStage<Void> submit(Runnable task) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        try{
            CompletableFuture.runAsync(()->{
                try {
                    publishBulkheadEvent(() -> new BulkheadOnCallPermittedEvent(name));
                    task.run();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, executorService).whenComplete((result, throwable) ->{
                publishBulkheadEvent(()-> new BulkheadOnCallRejectedEvent(name));
                if(throwable!=null){
                    promise.completeExceptionally(throwable);
                }else {
                    promise.complete(result);
                }
            });
        }catch(RejectedExecutionException e){
            publishBulkheadEvent(() -> new BulkheadOnCallRejectedEvent(name));
            throw e;
        }
        return promise;
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
        try{
            if(!executorService.awaitTermination(5,TimeUnit.SECONDS)){
                executorService.shutdownNow();
            }
        } catch (Exception e){
            if(!executorService.isTerminated()){
                executorService.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return String.format("FixedThreadPoolBulkhead '%s'", this.name);
    }

    private void publishBulkheadEvent(Supplier<BulkheadEvent> supplier){
        if(eventProcessor.hasConsumer()){
            eventProcessor.consumeEvent(supplier.get());
        }
    }

    private final class BulkheadMetrics implements Metrics {
        private BulkheadMetrics(){}

        @Override
        public int getCoreThreadPoolSize() {
            return executorService.getCorePoolSize();
        }

        @Override
        public int getThreadPoolSize() {
            return executorService.getPoolSize();
        }

        @Override
        public int getMaxThreadPoolSize() {
            return executorService.getMaximumPoolSize();
        }

        @Override
        public int getQueueDepth() {
            return executorService.getQueue().size();
        }

        @Override
        public int getRemainingQueueCapacity() {
            return executorService.getQueue().remainingCapacity();
        }

        @Override
        public int getQueueCapacity() {
            return config.getQueueCapacity();
        }

        @Override
        public int getActiveThreadCount() {
            return executorService.getActiveCount();
        }

        @Override
        public int getAvailableThreadCount() {
            return getMaxThreadPoolSize()-getActiveThreadCount();
        }
    }
    private static final class BulkheadEventProcessor extends EventProcessor<BulkheadEvent> implements ThreadPoolBulkheadEventPublisher,
            EventConsumer<BulkheadEvent> {
        @Override
        public ThreadPoolBulkheadEventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallRejectedEvent.class.getName(),eventConsumer);
            return this;
        }

        @Override
        public ThreadPoolBulkheadEventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallPermittedEvent.class.getName(),eventConsumer);
            return this;
        }

        @Override
        public ThreadPoolBulkheadEventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallFinishedEvent.class.getName(),eventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(BulkheadEvent event) {
            super.processEvent(event);
        }

    }
}
