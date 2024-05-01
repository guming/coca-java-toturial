package org.coca.ringbuffer;

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

public class ConcurrentEvictingQueue<E> extends AbstractQueue<E> {
    private static final int RETRIES = 5;
    private static final Object[] DEFAULT_DESTINATION = new Object[0];
    private static final String ILLEGAL_CAPACITY = "Capacity must be bigger than 0";

    private final int maxSize;
    private Object[] ringbuffer;
    private int headIndex;
    private int tailIndex;
    private AtomicInteger size;
    private StampedLock lock;

    public ConcurrentEvictingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(ILLEGAL_CAPACITY);
        }
        maxSize = capacity;
        size = new AtomicInteger(0);
        ringbuffer = new Object[capacity];
        headIndex = 0;
        tailIndex = 0;
        lock = new StampedLock();
    }

    @Override
    public Iterator<E> iterator() {
        return readConcurrently(()-> new Iter(headIndex));
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean offer(E e) {
        Supplier<Boolean> offerElement = () -> {
            if (size.get() == 0) {
                ringbuffer[tailIndex] = e;
                size.incrementAndGet();
            } else if (size.get() == maxSize) {
                headIndex = nextIndex(headIndex);
                tailIndex = nextIndex(tailIndex);
                ringbuffer[tailIndex] = e;
            } else {
                tailIndex = nextIndex(tailIndex);
                ringbuffer[tailIndex] = e;
                size.incrementAndGet();
            }
            return true;
        };
        return writeConcurrently(offerElement);
    }

    @Override
    public E poll() {
        Supplier<E> pollElement = () -> {
            if (size.get() == 0) {
                return null;
            }
            E result = (E) ringbuffer[headIndex];
            ringbuffer[headIndex] = null;
            if (size.get() != 1) {
                headIndex = nextIndex(headIndex);
            }
            size.decrementAndGet();
            return result;
        };
        return writeConcurrently(pollElement);
    }

    @Override
    public E peek() {
        Supplier<E> peekElement = () -> {
            if (size.get() == 0) {
                return null;
            }
            return (E) ringbuffer[headIndex];
        };
        return readConcurrently(peekElement);
    }

    @Override
    public Object[] toArray() {
        if (size.get() == 0) {
            return new Object[0];
        }
       return toArray(DEFAULT_DESTINATION);
    }

    @Override
    public void clear() {
        Supplier<Object> clearStrategy = () -> {
            if (size.get() == 0) {
                return null;
            }
            Arrays.fill(ringbuffer, null);
            size.set(0);
            headIndex = 0;
            tailIndex = 0;
            return null;
        };
        writeConcurrently(clearStrategy);
    }

    @Override
    public <T> T[] toArray(T[] target) {
        Supplier<T[]> copy = () -> {
            if (size.get() == 0) {
                return target;
            }
            T[] result = target;
            if(target.length<size.get()) {
                result = (T[])Array.newInstance(result.getClass().getComponentType(), size.get());
            }
            if (headIndex <= tailIndex) {
                System.arraycopy(ringbuffer, headIndex, result, 0, size.get());
            } else {
                int end = ringbuffer.length - headIndex;
                System.arraycopy(ringbuffer, headIndex, result, 0, end);
                System.arraycopy(ringbuffer, 0, result, end, tailIndex + 1);
            }
            return result;
        };
        return readConcurrentlyWithoutSpin(copy);
    }

    private int nextIndex(int ringIndex){
        int nextIndex = 1 + ringIndex;
        if(nextIndex == maxSize){
            nextIndex = 0;
        }
        return nextIndex;
    }

    private <T> T writeConcurrently(Supplier<T> supplier) {
        T result = null;
        long stamp = lock.writeLock();
        try{
            result = supplier.get();
        }finally {
            lock.unlockWrite(stamp);
        }
        return result;
    }

    private <T> T readConcurrently(Supplier<T> supplier) {
        T result = null;
        long stamp;
        for (int i = 0; i < RETRIES; i++) {
            stamp = lock.tryOptimisticRead();
            if(stamp == 0){
                continue;
            }
            result = supplier.get();
            if(lock.validate(stamp)){
                return result;
            }
        }
        stamp = lock.readLock();
        try{
            result = supplier.get();
        }finally {
            lock.unlockRead(stamp);
        }
        return result;
    }

    private <T> T readConcurrentlyWithoutSpin(final Supplier<T> readSupplier) {
        T result;
        long stamp = lock.readLock();
        try {
            result = readSupplier.get();
        } finally {
            lock.unlockRead(stamp);
        }
        return result;
    }

    private class Iter implements Iterator<E> {

        private int visitedCount = 0;
        private int cursor;

        public Iter(int headIndex) {
            this.cursor = headIndex;
        }

        @Override
        public boolean hasNext() {
            return visitedCount<size.get();
        }

        @Override
        public E next() {
            Supplier<E> nextElement = () -> {
                if (visitedCount >= size.get()) {
                    throw new NoSuchElementException();
                }
                E item = (E) ringbuffer[cursor];
                cursor = nextIndex(cursor);
                visitedCount++;
                return item;
            };
            return readConcurrently(nextElement);
        }
    }

}
