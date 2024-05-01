package coca.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventProcessor<T> implements EventPublisher<T> {
    private final Set<EventConsumer<T>> eventConsumers = new CopyOnWriteArraySet<>();
    private final ConcurrentHashMap<String, Set<EventConsumer<T>>> eventConsumerMap = new ConcurrentHashMap<>();
    private boolean consumerRegistered;

    @Override
    public synchronized void onEvent(EventConsumer<T> eventConsumer) {
        this.eventConsumers.add(eventConsumer);
        this.consumerRegistered = true;
    }

    @SuppressWarnings("unchecked")
    public synchronized void registerConsumer(String className, EventConsumer<? extends T> eventConsumer) {
        this.eventConsumerMap.compute(className, (key, consumers) -> {
            if (consumers == null) {
                consumers = new CopyOnWriteArraySet<>();
                consumers.add((EventConsumer<T>) eventConsumer);
                return consumers;
            } else {
                consumers.add((EventConsumer<T>) eventConsumer);
                return consumers;
            }
        });
        this.consumerRegistered = true;
    }

    public boolean hasConsumer() {
        return this.consumerRegistered;
    }

    public <E extends T> boolean processEvent(E event) {
        boolean consumed = false;
        if (!eventConsumers.isEmpty()) {
            for (EventConsumer<T> onEventConsumer : eventConsumers) {
                onEventConsumer.consumeEvent(event);
            }
            consumed = true;
        }
        if (!eventConsumerMap.isEmpty()) {
            Set<EventConsumer<T>> consumers = this.eventConsumerMap.get(event.getClass().getName());
            if (consumers != null && !consumers.isEmpty()) {
                for (EventConsumer<T> consumer : consumers) {
                    consumer.consumeEvent(event);
                }
                consumed = true;
            }
        }
        return consumed;
    }
}
