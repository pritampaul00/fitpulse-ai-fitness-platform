package com.fitness.aiservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityInsightItem {
    private String id;
    private String type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
}
