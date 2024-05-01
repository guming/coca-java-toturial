package org.coca.ringbuffer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConcurrentFifoBuffer<E> implements FifoBuffer<E> {

    private ConcurrentEvictingQueue<E> queue;
    private int capacity;

    public ConcurrentFifoBuffer(int capacity) {
        this.capacity = capacity;
        this.queue = new ConcurrentEvictingQueue<>(capacity);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }

    @Override
    public List<E> toList() {
        return List.copyOf(queue);
    }

    @Override
    public Stream<E> toStream() {
        return queue.stream();
    }

    @Override
    public void add(E element) {
        queue.offer(element);
    }

    @Override
    public Optional<E> take() {
        return Optional.ofNullable(queue.poll());
    }
}
