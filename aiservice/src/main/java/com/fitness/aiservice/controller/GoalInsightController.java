package com.fitness.aiservice.controller;

import com.fitness.aiservice.dto.GoalInsightResponse;
import com.fitness.aiservice.service.GoalInsightService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goal-insights")
@RequiredArgsConstructor
public class GoalInsightController {

    private final GoalInsightService goalInsightService;

    @PostMapping
    public ResponseEntity<GoalInsightResponse> generateInsights(
            @RequestHeader("X-User-ID") String userId) {

        return ResponseEntity.ok(
                goalInsightService.generateInsights(userId)
        );
    }
}
