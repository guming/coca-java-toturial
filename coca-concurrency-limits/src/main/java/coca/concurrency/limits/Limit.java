package coca.concurrency.limits;

import java.util.function.Consumer;

public interface Limit {
     int getLimit();
     void notifyOnChange(Consumer<Integer> consumer);
     void onSample(long startTime, long rtt, int inflight, boolean didDrop);
}
