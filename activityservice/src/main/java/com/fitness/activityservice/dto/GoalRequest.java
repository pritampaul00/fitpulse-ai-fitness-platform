package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.GoalType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalRequest {

    private String title;

    private String description;

    private GoalType goalType;

    private Double targetValue;

    private LocalDate startDate;

    private LocalDate endDate;
}
