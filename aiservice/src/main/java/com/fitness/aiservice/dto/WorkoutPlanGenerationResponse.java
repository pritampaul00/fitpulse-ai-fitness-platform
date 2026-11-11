package com.fitness.aiservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutPlanGenerationResponse {

    // AI-generated plan overview
    private String summary;

    // Generated workout sessions
    private List<GeneratedWorkoutSession> sessions;

    @Data
    public static class GeneratedWorkoutSession {

        private String title;

        private String description;

        private LocalDateTime scheduledAt;

        // Why AI selected this workout
        private String rationale;

        private List<GeneratedWorkoutExercise> exercises;
    }

    @Data
    public static class GeneratedWorkoutExercise {

        private String exerciseName;

        private Integer sets;

        private Integer reps;

        private Integer durationMinutes;

        private String notes;
    }
}