package coca.bulkhead.core;

public interface EventConsumer<T> {
    void consumeEvent(T event);
}
