package com.agrim.worker.repository;

import com.agrim.worker.model.WorkerDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerRepository extends MongoRepository<WorkerDocument, String> {

    List<WorkerDocument> findByStatus(String status);
}
