package org.coca.alg.structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;


public class WheelTimer implements Timer {

    private volatile long startTime;
    private final long tickDuration;

    private final Executor taskExecutor;
    private final Queue<HashedWheelTimeout> timeoutQueue = new LinkedBlockingQueue<>();
    private final Queue<HashedWheelTimeout> cancelledQueue = new LinkedBlockingQueue<>();

    private final Thread workerThread;
    private final Worker worker = new Worker();

    private final HashedWheelBucket[] wheelBuckets;

    private static final int WORKER_STATE_INIT = 0;
    private static final int WORKER_STATE_STARTED = 1;
    private static final int WORKER_STATE_SHUTDOWN = 2;

    private volatile int workerState;
    private static final AtomicIntegerFieldUpdater<WheelTimer> WORKER_STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(WheelTimer.class, "workerState");

    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);

    private final AtomicLong pendingTimeouts = new AtomicLong(0);
    private final long maxPendingTimeouts;

    public WheelTimer() {
        this(Executors.defaultThreadFactory());
    }

    public WheelTimer(long tickDuration, TimeUnit unit) {
        this(Executors.defaultThreadFactory(), tickDuration, unit);
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
        this.maxPendingTimeouts = maxPendingTimeouts;

    }

    public void start(){
        switch (WORKER_STATE_UPDATER.get(this)){
            case WORKER_STATE_INIT:
                if(WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)){
                    workerThread.start();
                }
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }
        if(startTime == 0){
            try{
                startTimeInitialized.await();
            } catch (InterruptedException ignored){
                //do nothing
            }
        }

    }

    @Override
    public Set<Timeout> stop() {
        if(Thread.currentThread() == workerThread){
            throw new IllegalStateException(WheelTimer.class.getSimpleName() + ".stop() cannot be called from");
        }
        if(!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)){
            if (WORKER_STATE_UPDATER.getAndSet(this,WORKER_STATE_SHUTDOWN)!=WORKER_STATE_SHUTDOWN){
                return Collections.emptySet();
            }
        }
        boolean interrupted = false;
        // workerThread shutdown
        while (workerThread.isAlive()){
            workerThread.interrupt();
            try {
                workerThread.join(100);
            } catch(InterruptedException ignored){
                interrupted = true;
            }
        }
        if(interrupted){
            Thread.currentThread().interrupt();
        }
        return worker.unprocessedTimeouts();
    }

    @Override
    public Timeout newTimeout(TimerTask timerTask, long delay, TimeUnit unit) {
        long pendingTimeoutsCount = pendingTimeouts.incrementAndGet();
        if (maxPendingTimeouts > 0 && pendingTimeoutsCount > maxPendingTimeouts) {
            pendingTimeouts.decrementAndGet();
            throw new RejectedExecutionException("Number of pending timeouts ("
                    + pendingTimeoutsCount + ") is greater than or equal to maximum allowed pending "
                    + "timeouts (" + maxPendingTimeouts + ")");
        }
        start();
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
        if (delay > 0 && deadline < 0) {
            deadline = Long.MAX_VALUE;
        }
        HashedWheelTimeout timeout = new HashedWheelTimeout(timerTask, this, deadline);
        timeoutQueue.add(timeout);
        return timeout;
    }

    public long pendingTimeouts() {
        return pendingTimeouts.get();
    }

    private static void reportTooManyInstances() {
        System.out.println("report Too Many Instances");
    }


    private static HashedWheelBucket[] createWheel(int ticksPerWheel){
        ticksPerWheel = findNextPositivePowerOfTwo(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i ++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    private static int findNextPositivePowerOfTwo(final int value) {
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
            startTimeInitialized.countDown();
            do {
                long deadline = waitForNextTick();
                if (deadline > 0) {
                    int idx = (int) tick & (wheelBuckets.length - 1);
                    processCancelledTasks();
                    HashedWheelBucket bucket = wheelBuckets[idx];
                    transferTimeoutsToBuckets();
                    bucket.expireTimeouts(deadline);
                    tick++;
                }
            } while (WORKER_STATE_UPDATER.get(WheelTimer.this) == WORKER_STATE_STARTED);

            for(HashedWheelBucket bucket: wheelBuckets){
                bucket.clearTimeout(unprocessedTimeouts);
            }

            for(;;){
                HashedWheelTimeout timeout = timeoutQueue.poll();
                if(timeout == null){
                    break;
                }
                if(!timeout.isCancelled()){
                    unprocessedTimeouts.add(timeout);
                }
            }
            processCancelledTasks();
        }
        private void transferTimeoutsToBuckets(){
            for (int i = 0; i < 100000; i++) {
                HashedWheelTimeout timeout = timeoutQueue.poll();
                if (timeout == null) {
                    break;
                }
                if(timeout.state == HashedWheelTimeout.CANCELLED){
                    continue;
                }
                long calculated = timeout.deadline / tickDuration;
                timeout.remainingRounds = (calculated - tick) / wheelBuckets.length;
                final long ticks = Math.max(calculated, tick);
                int stopIndex = (int) (ticks & (wheelBuckets.length-1));
                HashedWheelBucket bucket = wheelBuckets[stopIndex];
                bucket.addTimeout(timeout);
            }
        }
        private void processCancelledTasks(){
            for (;;) {
                HashedWheelTimeout timeout = cancelledQueue.poll();
                if (timeout == null) {
                    break;
                }
                try {
                    timeout.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);
            for (;;) {
                long currentTime = System.nanoTime() - startTime;
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;
                if (sleepTimeMs <= 0) {
                    if (currentTime == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        return currentTime;
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
        HashedWheelBucket bucket;


        public HashedWheelTimeout(TimerTask timerTask, WheelTimer timer, long deadline) {
            this.task = timerTask;
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

        public void remove(){
            HashedWheelBucket bucket = this.bucket;
            if (bucket == null) {
                timer.pendingTimeouts.decrementAndGet();
            }else {
                bucket.remove(this);
            }
        }
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
            timeout.timer.pendingTimeouts.decrementAndGet();
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
            for (;;) {
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
