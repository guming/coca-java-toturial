package org.coca.alg.structure;


import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class WheelTimerTest {
    @Test
    public void testScheduleTimeoutShouldNotRunBeforeDelay() throws InterruptedException {
        final Timer timer = new WheelTimer();
        final CountDownLatch barrier = new CountDownLatch(1);
        final Timeout timeout = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                fail("This should not have run");
                barrier.countDown();
            }
        }, 10, TimeUnit.SECONDS);
        assertFalse(barrier.await(3, TimeUnit.SECONDS));
        assertFalse(timeout.isExpired(), "timer should not expire");
        timer.stop();
    }

    @Test
    public void testScheduleTimeoutShouldRunAfterDelay() throws InterruptedException {
        final Timer timer = new WheelTimer();
        final CountDownLatch barrier = new CountDownLatch(1);
        final Timeout timeout = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                barrier.countDown();
            }
        }, 2, TimeUnit.SECONDS);
        assertTrue(barrier.await(3, TimeUnit.SECONDS));
        assertTrue(timeout.isExpired(), "timer should expire");
        timer.stop();
    }

    @Test
    @org.junit.jupiter.api.Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void testStopTimer() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final Timer timerProcessed = new WheelTimer();
        for (int i = 0; i < 3; i ++) {
            timerProcessed.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {
                    latch.countDown();
                }
            }, 1, TimeUnit.MILLISECONDS);
        }

        latch.await();
        assertEquals(0, timerProcessed.stop().size(), "Number of unprocessed timeouts should be 0");

        final Timer timerUnprocessed = new WheelTimer();
        for (int i = 0; i < 5; i ++) {
            timerUnprocessed.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                }
            }, 5, TimeUnit.SECONDS);
        }
        Thread.sleep(1000L); // sleep for a second
        assertFalse(timerUnprocessed.stop().isEmpty(), "Number of unprocessed timeouts should be greater than 0");
    }

    @Test
    @org.junit.jupiter.api.Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void testTimerShouldThrowExceptionAfterShutdownForNewTimeouts() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);
        final Timer timer = new WheelTimer();
        for (int i = 0; i < 3; i ++) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    latch.countDown();
                }
            }, 1, TimeUnit.MILLISECONDS);
        }

        latch.await();
        timer.stop();

        try {
            timer.newTimeout(createNoOpTimerTask(), 1, TimeUnit.MILLISECONDS);
            fail("Expected exception didn't occur.");
        } catch (IllegalStateException ignored) {
            // expected
        }
    }

    @Test
    @org.junit.jupiter.api.Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
    public void testTimerOverflowWheelLength() throws InterruptedException {
        final WheelTimer timer = new WheelTimer(
                Executors.defaultThreadFactory(), 100, TimeUnit.MILLISECONDS, 32);
        final CountDownLatch latch = new CountDownLatch(3);

        timer.newTimeout(new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
                timer.newTimeout(this, 100, TimeUnit.MILLISECONDS);
                latch.countDown();
            }
        }, 100, TimeUnit.MILLISECONDS);

        latch.await();
        assertFalse(timer.stop().isEmpty());
    }

    @Test
    public void testExecutionOnTime() throws InterruptedException {
        int tickDuration = 200;
        int timeout = 125;
        int maxTimeout = 2 * (tickDuration + timeout);
        final WheelTimer timer = new WheelTimer(tickDuration, TimeUnit.MILLISECONDS);
        final BlockingQueue<Long> queue = new LinkedBlockingQueue<Long>();

        int scheduledTasks = 100000;
        for (int i = 0; i < scheduledTasks; i++) {
            final long start = System.nanoTime();
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {
                    queue.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
                }
            }, timeout, TimeUnit.MILLISECONDS);
        }

        for (int i = 0; i < scheduledTasks; i++) {
            long delay = queue.take();
            assertTrue(delay >= timeout && delay < maxTimeout,
                    "Timeout + " + scheduledTasks + " delay " + delay + " must be " + timeout + " < " + maxTimeout);
        }

        timer.stop();
    }

    @Test
    public void testExecutionOnTaskExecutor() throws InterruptedException {
        int timeout = 10;

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch timeoutLatch = new CountDownLatch(1);
        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                try {
                    command.run();
                } finally {
                    latch.countDown();
                }
            }
        };
        final WheelTimer timer = new WheelTimer(Executors.defaultThreadFactory(), 100,
                TimeUnit.MILLISECONDS, 32, true, 2, executor);
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
                timeoutLatch.countDown();
            }
        }, timeout, TimeUnit.MILLISECONDS);

        latch.await();
        timeoutLatch.await();
        timer.stop();
    }

    @Test
    public void testRejectedExecutionExceptionWhenTooManyTimeoutsAreAddedBackToBack() {
        WheelTimer timer = new WheelTimer(Executors.defaultThreadFactory(), 100,
                TimeUnit.MILLISECONDS, 32, true, 2);
        timer.newTimeout(createNoOpTimerTask(), 5, TimeUnit.SECONDS);
        timer.newTimeout(createNoOpTimerTask(), 5, TimeUnit.SECONDS);
        try {
            timer.newTimeout(createNoOpTimerTask(), 1, TimeUnit.MILLISECONDS);
            fail("Timer allowed adding 3 timeouts when maxPendingTimeouts was 2");
        } catch (RejectedExecutionException e) {
            // Expected
        } finally {
            timer.stop();
        }
    }

    @Test
    public void testNewTimeoutShouldStopThrowingRejectedExecutionExceptionWhenExistingTimeoutIsCancelled()
            throws InterruptedException {
        final int tickDurationMs = 100;
        final WheelTimer timer = new WheelTimer(Executors.defaultThreadFactory(), tickDurationMs,
                TimeUnit.MILLISECONDS, 32, true, 2);
        timer.newTimeout(createNoOpTimerTask(), 5, TimeUnit.SECONDS);
        Timeout timeoutToCancel = timer.newTimeout(createNoOpTimerTask(), 5, TimeUnit.SECONDS);
        assertTrue(timeoutToCancel.cancel());

        Thread.sleep(tickDurationMs * 5);

        final CountDownLatch secondLatch = new CountDownLatch(1);
        timer.newTimeout(createCountDownLatchTimerTask(secondLatch), 90, TimeUnit.MILLISECONDS);

        secondLatch.await();
        timer.stop();
    }

    @Test
    @org.junit.jupiter.api.Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    public void testNewTimeoutShouldStopThrowingRejectedExecutionExceptionWhenExistingTimeoutIsExecuted()
            throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final WheelTimer timer = new WheelTimer(Executors.defaultThreadFactory(), 25,
                TimeUnit.MILLISECONDS, 4, true, 2);
        timer.newTimeout(createNoOpTimerTask(), 5, TimeUnit.SECONDS);
        timer.newTimeout(createCountDownLatchTimerTask(latch), 90, TimeUnit.MILLISECONDS);

        latch.await();

        final CountDownLatch secondLatch = new CountDownLatch(1);
        timer.newTimeout(createCountDownLatchTimerTask(secondLatch), 90, TimeUnit.MILLISECONDS);

        secondLatch.await();
        timer.stop();
    }

//    @Test()
//    public void reportPendingTimeouts() throws InterruptedException {
//        final CountDownLatch latch = new CountDownLatch(1);
//        final WheelTimer timer = new WheelTimer();
//        final Timeout t1 = timer.newTimeout(createNoOpTimerTask(), 100, TimeUnit.MINUTES);
//        final Timeout t2 = timer.newTimeout(createNoOpTimerTask(), 100, TimeUnit.MINUTES);
//        timer.newTimeout(createCountDownLatchTimerTask(latch), 90, TimeUnit.MILLISECONDS);
//
//        assertEquals(3, timer.pendingTimeouts());
//        t1.cancel();
//        t2.cancel();
//        latch.await();
//
//        assertEquals(0, timer.pendingTimeouts());
//        timer.stop();
//    }

    @Test
    public void testOverflow() throws InterruptedException  {
        final WheelTimer timer = new WheelTimer();
        final CountDownLatch latch = new CountDownLatch(1);
        Timeout timeout = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                latch.countDown();
            }
        }, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        assertFalse(latch.await(1, TimeUnit.SECONDS));
        timeout.cancel();
        timer.stop();
    }

    private static TimerTask createNoOpTimerTask() {
        return new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
            }
        };
    }

    private static TimerTask createCountDownLatchTimerTask(final CountDownLatch latch) {
        return new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
                latch.countDown();
            }
        };
    }
}