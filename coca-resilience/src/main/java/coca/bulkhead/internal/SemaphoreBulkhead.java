package coca.bulkhead.internal;

import coca.bulkhead.Bulkhead;
import coca.bulkhead.BulkheadConfig;
import coca.core.EventConsumer;
import coca.core.EventProcessor;
import coca.bulkhead.event.BulkheadEvent;
import coca.bulkhead.event.BulkheadOnCallFinishedEvent;
import coca.bulkhead.event.BulkheadOnCallPermittedEvent;
import coca.bulkhead.event.BulkheadOnCallRejectedEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public class SemaphoreBulkhead implements Bulkhead {

    private static final String CONFIG_MUST_NOT_BE_NULL = "Config must not be null";
    private static final String TAGS_MUST_NOTE_BE_NULL = "Tags must not be null";
    private final Map<String, String> tags;
    private final String name;
    private final Semaphore semaphore;
    private final BulkheadConfig config;

    private final Metrics metrics;
    private final BulkheadEventProcessor eventProcessor;

    public SemaphoreBulkhead(Map<String, String> tags, String name, BulkheadConfig bulkheadConfig) {
        this.tags = Objects.requireNonNull(tags, TAGS_MUST_NOTE_BE_NULL);
        this.name = name;
        this.config = Objects.requireNonNull(bulkheadConfig, CONFIG_MUST_NOT_BE_NULL);
        this.semaphore = new Semaphore(config.getMaxConcurrentCalls(), config.isFairCallHandlingEnabled());
        this.metrics = new BulkheadMetrics();
        this.eventProcessor = new BulkheadEventProcessor();
    }

    public SemaphoreBulkhead(String name) {
        this(emptyMap(), name, BulkheadConfig.ofDefaults());
    }

    public boolean tryEnterBulkhead() {
        long timeout = config.getMaxWaitDuration().toMillis();
        boolean res = false;
        try {
            res = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return res;
    }
    @Override
    public boolean tryAcquirePermission() {
        boolean callPermitted = tryEnterBulkhead();
        publishBulkheadEvent(
                () -> callPermitted ? new BulkheadOnCallPermittedEvent(name)
                        : new BulkheadOnCallRejectedEvent(name)
        );
        return callPermitted;
    }
    @Override
    public void acquirePermission() {
        boolean permitted = tryAcquirePermission();
        if(permitted){
            return;
        }
        if(Thread.currentThread().isInterrupted()){
            throw new RuntimeException("thread has been interrupted");
        }
        throw new RuntimeException("there is no permission");
    }

    @Override
    public void releasePermission() {
        semaphore.release();
    }

    @Override
    public void onComplete() {
        semaphore.release();
        publishBulkheadEvent(()->new BulkheadOnCallFinishedEvent(name));
    }

    private void publishBulkheadEvent(Supplier<BulkheadEvent> eventSupplier) {
        if (eventProcessor.hasConsumer()) {
            eventProcessor.consumeEvent(eventSupplier.get());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return eventProcessor;
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }
    @Override
    public BulkheadConfig getBulkheadConfig() {
        return config;
    }

    private static final class BulkheadEventProcessor extends EventProcessor<BulkheadEvent> implements EventPublisher,
            EventConsumer<BulkheadEvent> {
        @Override
        public EventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallRejectedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallPermittedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public EventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer) {
            registerConsumer(BulkheadOnCallFinishedEvent.class.getName(), eventConsumer);
            return this;
        }

        @Override
        public void consumeEvent(BulkheadEvent event) {
            super.processEvent(event);
        }
    }
    private final  class BulkheadMetrics implements Metrics{
        private BulkheadMetrics() {
        }
        @Override
        public int getAvailableConcurrentCalls() {
            return semaphore.availablePermits();
        }

        @Override
        public int getMaxAllowedConcurrentCalls() {
            return config.getMaxConcurrentCalls();
        }
    }
}
