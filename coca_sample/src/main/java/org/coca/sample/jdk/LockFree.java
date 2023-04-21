package org.coca.sample.jdk;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

public class LockFree<T> {
    class Node<T> {
        volatile Node prev;
        volatile Node next;
        T value;
        public Node(T value) {
            this.value = value;
            this.next = null;
            this.prev = null;
        }
    }
    AtomicReference<Node<T>> head;
    AtomicReference<Node<T>> tail;

    public LockFree() {
        head = new AtomicReference<>(null);
        tail = new AtomicReference<>(null);
    }

    public boolean add(T o) {
        Node node = new Node(o);
        boolean flag = false;

        for(;;) {
            Node currentTail =tail.get();
            node.prev = currentTail;
            if (currentTail == tail.get()) {//tail no changed
                if(tail.compareAndSet(currentTail, node)){
                    flag = true;
                    break;
                }
            }
        }
        // update node next
        if(node.prev!=null){
            node.prev.next = node;
        }
        //if head is null,first update
        head.compareAndSet(null, node);
        return flag;
    }

    public boolean offer(T o) {
        return add(o);
    }

    public T poll() {
        if(head.get() == null){
            return null;
        }
        for(;;){
            Node currentHead = head.get();
            Node next = currentHead.next;
            if (currentHead == head.get()) {//head no changed
                T value = (T) currentHead.value;
                if (head.compareAndSet(currentHead, next)) {
                    return value;
                }
            }
        }
    }

}
