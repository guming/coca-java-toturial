package org.coca.sample.jdk;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        LockFree<String> queue = new LockFree<>();
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            Thread thread = new Thread(() -> {
                System.out.println("start thread " + finalI);
                System.out.println("add flag:"+finalI+","+queue.offer("insert value " + finalI));
            });
            thread.start();
        }
        Thread.sleep(1000*1);
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> System.out.println(queue.poll()));
            thread.start();
        }
    }
}
