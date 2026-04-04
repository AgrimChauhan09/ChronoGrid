package com.agrim.queueservice.repository;

import com.agrim.queueservice.model.QueueItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueItemRepository extends MongoRepository<QueueItemDocument, String> {

    List<QueueItemDocument> findByStatusOrderByEnqueuedAtAsc(String status);

    long countByStatus(String status);
}
