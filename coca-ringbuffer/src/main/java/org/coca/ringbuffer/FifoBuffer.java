package org.coca.ringbuffer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FifoBuffer<T> {

    int size();

    boolean isEmpty();

    boolean isFull();

    List<T> toList();

    Stream<T> toStream();

    void add(T element);

    Optional<T> take();

}
