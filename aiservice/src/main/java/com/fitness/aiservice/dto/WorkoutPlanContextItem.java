package com.fitness.aiservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutPlanContextItem {

    private String id;

    private String title;

    private String description;

    private LocalDateTime scheduledAt;

    private String status;

    private List<WorkoutExerciseContextItem> exercises;

    @Data
    public static class WorkoutExerciseContextItem {

        private String exerciseName;

        private Integer sets;

        private Integer reps;

        private Integer durationMinutes;

        private String notes;
    }
}