package com.agrim.common.queue;

import com.agrim.common.model.Job;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * SimpleJobQueue — replaces Apache Kafka / RabbitMQ.
 * Uses Java's LinkedBlockingQueue for in-process job queuing.
 * Jobs are also persisted in MongoDB for durability.
 */
@Component
public class SimpleJobQueue {

    private final LinkedBlockingQueue<Job> internalQueue;

    private static final int DEFAULT_CAPACITY = 1000;

    public SimpleJobQueue() {
        this.internalQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
    }

    public boolean enqueue(Job job) {
        return internalQueue.offer(job);
    }

    public Job dequeue() throws InterruptedException {
        return internalQueue.poll(5, TimeUnit.SECONDS);
    }

    public Job dequeueNow() {
        return internalQueue.poll();
    }

    public int size() {
        return internalQueue.size();
    }

    public boolean isEmpty() {
        return internalQueue.isEmpty();
    }

    public int remainingCapacity() {
        return internalQueue.remainingCapacity();
    }
}
