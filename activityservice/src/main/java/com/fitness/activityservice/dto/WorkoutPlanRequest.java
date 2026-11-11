package com.fitness.activityservice.dto;

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
public class WorkoutPlanRequest {

    private String title;

    private String description;

    private LocalDateTime scheduledAt;

    private List<WorkoutExerciseRequest> exercises;
}