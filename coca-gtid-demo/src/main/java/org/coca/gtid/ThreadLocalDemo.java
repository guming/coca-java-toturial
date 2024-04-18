package org.coca.gtid;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadLocalDemo {
    private static final AtomicLong nextId = new AtomicLong(10);
    private static final ThreadLocal<Long> threadId = InheritableThreadLocal.withInitial(()->nextId.getAndIncrement());

    public static void main(String[] args) throws InterruptedException {
        set();
        System.out.println("main:"+get());
        Thread thread = new Thread(() -> {
            System.out.println("sub-thread:"+threadId.get());
            threadId.set(5L);
            System.out.println("sub-thread:"+threadId.get());
        }
        );
        thread.start();
        thread.join();
        System.out.println("main:"+get());
    }
    public static Long get() {
        return threadId.get();
    }
    public static void set() {
         threadId.set(Thread.currentThread().getId());
    }

}
