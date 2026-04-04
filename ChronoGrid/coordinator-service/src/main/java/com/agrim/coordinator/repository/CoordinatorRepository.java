package com.agrim.coordinator.repository;

import com.agrim.coordinator.model.CoordinatorDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordinatorRepository extends MongoRepository<CoordinatorDocument, String> {

    List<CoordinatorDocument> findByJobId(String jobId);
}
