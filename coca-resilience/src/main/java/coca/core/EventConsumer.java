package coca.core;

public interface EventConsumer<T> {
    void consumeEvent(T event);

}
