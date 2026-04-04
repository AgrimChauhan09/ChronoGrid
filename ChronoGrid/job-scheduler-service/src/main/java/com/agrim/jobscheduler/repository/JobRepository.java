package com.agrim.jobscheduler.repository;

import com.agrim.common.model.JobStatus;
import com.agrim.jobscheduler.model.JobDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<JobDocument, String> {

    List<JobDocument> findByStatus(JobStatus status);

    List<JobDocument> findByType(String type);

    long countByStatus(JobStatus status);
}
