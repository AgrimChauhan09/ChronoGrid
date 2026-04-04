package com.agrim.queueservice.service;

import com.agrim.common.model.Job;
import com.agrim.common.queue.SimpleJobQueue;
import com.agrim.common.queue.QueueStats;
import com.agrim.queueservice.model.QueueItemDocument;
import com.agrim.queueservice.repository.QueueItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * QueueService — wraps SimpleJobQueue with MongoDB persistence.
 * Maps to pkg/queue/queue_service.go and queue.go in the Go project.
 */
@Service
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final SimpleJobQueue jobQueue;
    private final QueueItemRepository queueItemRepository;

    private final AtomicLong totalEnqueued = new AtomicLong(0);
    private final AtomicLong totalDequeued = new AtomicLong(0);

    public QueueService(SimpleJobQueue jobQueue, QueueItemRepository queueItemRepository) {
        this.jobQueue = jobQueue;
        this.queueItemRepository = queueItemRepository;
    }

    public boolean enqueue(Job job) {
        boolean accepted = jobQueue.enqueue(job);

        if (accepted) {
            totalEnqueued.incrementAndGet();
            QueueItemDocument item = QueueItemDocument.builder()
                    .jobId(job.getId())
                    .jobType(job.getType())
                    .status("QUEUED")
                    .enqueuedAt(Instant.now())
                    .build();
            queueItemRepository.save(item);
            log.info("Job {} enqueued", job.getId());
        } else {
            log.warn("Queue full — job {} dropped", job.getId());
        }

        return accepted;
    }

    public Job dequeue() throws InterruptedException {
        Job job = jobQueue.dequeue();
        if (job != null) {
            totalDequeued.incrementAndGet();
            log.debug("Job {} dequeued", job.getId());
        }
        return job;
    }

    public QueueStats getStats() {
        return QueueStats.builder()
                .currentSize(jobQueue.size())
                .capacity(1000)
                .remainingCapacity(jobQueue.remainingCapacity())
                .totalEnqueued(totalEnqueued.get())
                .totalDequeued(totalDequeued.get())
                .build();
    }
}
