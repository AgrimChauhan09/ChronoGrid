package com.agrim.watcher.repository;

import com.agrim.watcher.model.WatcherDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatcherRepository extends MongoRepository<WatcherDocument, String> {

    List<WatcherDocument> findByJobId(String jobId);
}
