package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.GoalStatus;
import com.fitness.activityservice.model.GoalType;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class GoalResponse {

    private String id;

    private String title;

    private String description;

    private GoalType goalType;

    private Double targetValue;

    private Double currentValue;

    private Double progressPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private GoalStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
