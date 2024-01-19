package org.coca.sample.jdk;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TwoPointLock implements Lock {
    Semaphore semaphore =new Semaphore(2);
    @Override
    public void lock() {
        semaphore.acquireUninterruptibly();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public boolean tryLock() {
        return semaphore.tryAcquire();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(time,unit);
    }

    @Override
    public void unlock() {
        semaphore.release();
    }

    @Override
    public Condition newCondition() {

        return null;
    }
}
