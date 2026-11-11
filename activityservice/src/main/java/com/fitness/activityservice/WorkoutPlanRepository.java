package com.fitness.activityservice;

import com.fitness.activityservice.model.WorkoutPlan;
import com.fitness.activityservice.model.WorkoutPlanStatus;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository
        extends MongoRepository<WorkoutPlan, String> {

    List<WorkoutPlan> findByUserIdOrderByScheduledAtAsc(String userId);

    Optional<WorkoutPlan> findByIdAndUserId(
            String id,
            String userId
    );

    List<WorkoutPlan> findByUserIdAndScheduledAtBetweenOrderByScheduledAtAsc(
            String userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<WorkoutPlan> findByUserIdAndStatusOrderByScheduledAtAsc(
            String userId,
            WorkoutPlanStatus status
    );
}