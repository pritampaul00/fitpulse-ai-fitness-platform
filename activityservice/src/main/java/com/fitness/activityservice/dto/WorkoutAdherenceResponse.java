package com.fitness.activityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutAdherenceResponse {

    private int periodDays;
    private long totalScheduled;
    private long completed;
    private long pending;
    private long cancelled;
    private double adherencePercentage;
}