package org.coca.alg.structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


public class WheelTimer implements Timer {

    private volatile long startTime;
    private final long tickDuration;

    private final Executor taskExecutor;
    private final Queue<HashedWheelTimeout> timeoutQueue = new LinkedBlockingQueue<>();
    private final Queue<HashedWheelTimeout> cancelledQueue = new LinkedBlockingQueue<>();

    private final Thread workerThread;
    private final Worker worker = new Worker();

    private final HashedWheelBucket[] wheelBuckets;

    private static HashedWheelBucket[] createWheel(int ticksPerWheel){
        ticksPerWheel = findNextPositivePowerOfTwo(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i ++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    public static int findNextPositivePowerOfTwo(final int value) {
        assert value > Integer.MIN_VALUE && value < 0x40000000;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }


    private final class Worker implements Runnable {
        private final Set<Timeout> unprocessedTimeouts = new HashSet<>();
        private long tick;

        @Override
        public void run() {
            startTime = System.nanoTime();
            if (startTime == 0) {
                startTime = 1;
            }
            long deadline = waitForNextTick();
            if (deadline > 0) {
                int idx = (int) tick & (wheelBuckets.length - 1);
                HashedWheelBucket bucket = wheelBuckets[idx];
                bucket.expireTimeouts(deadline);
                tick++;
            }
        }

        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);
            for (;;) {
                long current = System.nanoTime() - startTime;
                long sleepTimeMs = (deadline - current + 999999) / 1000000;
                if (sleepTimeMs <= 0) {
                    if (current == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        return current;
                    }
                }
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public Set<Timeout> unprocessedTimeouts() {
            return Collections.unmodifiableSet(unprocessedTimeouts);
        }
    }


    public WheelTimer() {
        this(Executors.defaultThreadFactory());
    }

    public WheelTimer(ThreadFactory threadFactory) {
        this(threadFactory, 100, TimeUnit.MILLISECONDS);
    }

    public WheelTimer(
            ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
        this(threadFactory, tickDuration, unit, 512);
    }

    public WheelTimer(
            ThreadFactory threadFactory,
            long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(threadFactory, tickDuration, unit, ticksPerWheel, true);
    }

    public WheelTimer(
            ThreadFactory threadFactory,
            long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection) {
        this(threadFactory, tickDuration, unit, ticksPerWheel, leakDetection, -1);
    }

    public WheelTimer(
            ThreadFactory threadFactory,
            long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection,
            long maxPendingTimeouts) {
        this(threadFactory, tickDuration, unit, ticksPerWheel, leakDetection,
                maxPendingTimeouts, ImmediateExecutor.INSTANCE);
    }

    public WheelTimer(
            ThreadFactory threadFactory,
            long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection,
            long maxPendingTimeouts, Executor taskExecutor) {

        this.taskExecutor = taskExecutor;
        this.tickDuration = unit.toNanos(tickDuration);
        workerThread = threadFactory.newThread(worker);
        wheelBuckets = createWheel(ticksPerWheel);
    }

    private static final class HashedWheelTimeout implements Timeout, Runnable {

        private TimerTask task;
        private static final int INIT = 0;
        private static final int CANCELLED = 1;
        private static final int EXPIRED = 2;
        private volatile int state = INIT;
        private final WheelTimer timer;
        private final long deadline;
        long remainingRounds;
        private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");
        HashedWheelTimeout next;
        HashedWheelTimeout prev;
        HashedWheelBucket[] bucket;


        public HashedWheelTimeout(TimerTask task, WheelTimer timer, long deadline) {
            this.task = task;
            this.timer = timer;
            this.deadline = deadline;
        }

        @Override
        public void run() {
            try {
                task.run(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public int state() {
            return state;
        }

        @Override
        public Timer timer() {
            return timer;
        }

        @Override
        public TimerTask task() {
            return task;
        }

        @Override
        public boolean isExpired() {
            return EXPIRED == state();
        }

        @Override
        public boolean isCancelled() {
            return EXPIRED == state();
        }

        public boolean compareAndSetState(int expected, int state) {
            return STATE_UPDATER.compareAndSet(this, expected, state);
        }

        public boolean cancel() {
            if (!compareAndSetState(INIT, CANCELLED)) {
                return false;
            }
            timer.cancelledQueue.add(this);
            return true;
        }

        public void expire() {
            if (!compareAndSetState(INIT, EXPIRED)) {
                return;
            }
            timer.taskExecutor.execute(this);
        }
    }

    @Override
    public Timeout newTimeout(TimerTask timerTask, long delay, TimeUnit unit) {
        return null;
    }

    @Override
    public Set<Timeout> stop() {
        return null;
    }

    private static final class HashedWheelBucket {
        private HashedWheelTimeout head;
        private HashedWheelTimeout tail;

        public void addTimeout(HashedWheelTimeout timeout) {
            if (head == null) {
                head = tail = timeout;
            } else {
                tail.next = timeout;
                timeout.prev = tail;
                tail = timeout;
            }
        }

        public void expireTimeouts(long deadline) {
            HashedWheelTimeout timeout = head;
            while (timeout != null) {
                HashedWheelTimeout next = timeout.next;
                if (timeout.remainingRounds <= 0) {
                    next = remove(timeout);
                    if (timeout.deadline <= deadline) {
                        timeout.expire();
                    } else {
                        //do nothing
                    }
                } else if (timeout.isCancelled()) {
                    next = remove(timeout);
                } else {
                    timeout.remainingRounds--;
                }
                timeout = next;
            }
        }

        public HashedWheelTimeout remove(HashedWheelTimeout timeout) {
            HashedWheelTimeout next = timeout.next;
            if (timeout.prev != null) {
                timeout.prev.next = next;
            }
            if (timeout.next != null) {
                timeout.next.prev = timeout.prev;
            }
            if (timeout == head) {
                if (timeout == tail) {
                    head = null;
                    tail = null;
                } else {
                    head.next = next;
                }
            } else {
                if (timeout == tail) {
                    tail = timeout.prev;
                }
            }
            timeout.prev = null;
            timeout.next = null;
            timeout.bucket = null;
            return next;
        }

        private HashedWheelTimeout pollTimeout() {
            HashedWheelTimeout head = this.head;
            if (head == null) {
                return null;
            }
            HashedWheelTimeout next = head.next;
            if (next == null) {
                tail = this.head = null;
            } else {
                this.head = next;
                next.prev = null;
            }
            head.prev = null;
            head.next = null;
            head.bucket = null;
            return head;
        }

        public void clearTimeout(Set<Timeout> set) {
            for (; ; ) {
                HashedWheelTimeout timeout = pollTimeout();
                if (timeout == null) {
                    return;
                }
                if (timeout.isExpired() || timeout.isCancelled()) {
                    continue;
                }
                set.add(timeout);
            }
        }
    }
}
