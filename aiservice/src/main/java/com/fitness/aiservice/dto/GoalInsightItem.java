package com.fitness.aiservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GoalInsightItem {
    private String id;
    private String title;
    private String description;
    private String goalType;
    private Double targetValue;
    private Double currentValue;
    private Double progressPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
