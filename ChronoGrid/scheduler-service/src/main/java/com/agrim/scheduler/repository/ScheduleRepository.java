package com.agrim.scheduler.repository;

import com.agrim.scheduler.model.ScheduleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<ScheduleDocument, String> {

    List<ScheduleDocument> findByActiveTrue();

    List<ScheduleDocument> findByJobType(String jobType);
}
