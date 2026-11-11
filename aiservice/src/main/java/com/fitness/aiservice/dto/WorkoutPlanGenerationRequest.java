package com.fitness.aiservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class WorkoutPlanGenerationRequest {

    // Number of workout sessions per week
    private Integer daysPerWeek;

    // Desired duration of each workout session
    private Integer sessionDurationMinutes;

    // BEGINNER, INTERMEDIATE, ADVANCED
    private String fitnessLevel;

    // Available equipment
    private List<String> equipment;

    // Available training days
    // Example: MONDAY, WEDNESDAY, FRIDAY
    private List<String> availableDays;

    // Preferred workout start time
    private LocalTime preferredStartTime;

    // Date from which the generated plan should begin
    private LocalDate startDate;

    // Optional training focus
    // Example: MUSCLE_GAIN, WEIGHT_LOSS, STRENGTH, GENERAL_FITNESS
    private String workoutFocus;

    // Optional additional instructions
    private String preferences;
}