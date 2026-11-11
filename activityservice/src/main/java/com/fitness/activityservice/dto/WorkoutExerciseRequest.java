package com.fitness.activityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutExerciseRequest {

    private String exerciseName;

    private Integer sets;

    private Integer reps;

    private Integer durationMinutes;

    private String notes;
}