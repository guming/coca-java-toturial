package coca.bulkhead.core;

public interface EventPublisher<T> {
    void onEvent(EventConsumer<T> eventConsumer);
}
