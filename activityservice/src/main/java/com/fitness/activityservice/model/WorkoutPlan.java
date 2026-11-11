package com.fitness.activityservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "workout_plans")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutPlan {

    @Id
    private String id;

    private String userId;

    private String title;

    private String description;

    private LocalDateTime scheduledAt;

    @Builder.Default
    private WorkoutPlanStatus status = WorkoutPlanStatus.PLANNED;

    private List<WorkoutExercise> exercises;

    private String completedActivityId;

    private LocalDateTime completedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}