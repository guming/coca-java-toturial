package coca.concurrency.limits.limit;

import coca.concurrency.limits.Limit;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class AbstractLimit implements Limit {
    private volatile int limit;
    private List<Consumer<Integer>> listeners = new CopyOnWriteArrayList<>();

    public AbstractLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public void notifyOnChange(Consumer<Integer> consumer) {
        this.listeners.add(consumer);
    }

    @Override
    public synchronized void onSample(long startTime, long rtt, int inflight, boolean didDrop) {
        int newLimit = _update(startTime, rtt, inflight, didDrop);
        if (newLimit != limit) {
            limit = newLimit;
            listeners.forEach(listener -> listener.accept(newLimit));
        }
    }
    protected abstract int _update(long startTime, long rtt, int inflight, boolean didDrop);
}
