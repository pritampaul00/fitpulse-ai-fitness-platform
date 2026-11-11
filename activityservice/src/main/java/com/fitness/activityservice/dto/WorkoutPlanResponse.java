package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.WorkoutPlanStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutPlanResponse {

    private String id;

    private String title;

    private String description;

    private LocalDateTime scheduledAt;

    private WorkoutPlanStatus status;

    private List<WorkoutExerciseResponse> exercises;

    private String completedActivityId;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}