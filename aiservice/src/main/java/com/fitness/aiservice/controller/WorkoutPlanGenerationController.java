package com.fitness.aiservice.controller;

import com.fitness.aiservice.dto.WorkoutPlanGenerationRequest;
import com.fitness.aiservice.dto.WorkoutPlanGenerationResponse;
import com.fitness.aiservice.service.WorkoutPlanGenerationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/workout-plans")
@RequiredArgsConstructor
@Slf4j
public class WorkoutPlanGenerationController {

    private final WorkoutPlanGenerationService workoutPlanGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<WorkoutPlanGenerationResponse> generateWorkoutPlan(
            @RequestBody WorkoutPlanGenerationRequest request,
            @RequestHeader("X-User-ID") String userId) {

        log.info("Received AI workout generation request for user: {}", userId);

        WorkoutPlanGenerationResponse response =
                workoutPlanGenerationService.generateWorkoutPlan(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}