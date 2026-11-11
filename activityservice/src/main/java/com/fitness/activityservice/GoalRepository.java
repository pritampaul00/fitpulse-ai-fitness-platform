package com.fitness.activityservice;

import com.fitness.activityservice.model.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends MongoRepository<Goal, String> {

    List<Goal> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Goal> findByIdAndUserId(String id, String userId);
}
