package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.GoalRequest;
import com.fitness.activityservice.dto.GoalResponse;
import com.fitness.activityservice.service.GoalService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody GoalRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(goalService.createGoal(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getUserGoals(
            @RequestHeader("X-User-ID") String userId) {

        return ResponseEntity.ok(
                goalService.getUserGoals(userId)
        );
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalResponse> getGoal(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String goalId) {

        return ResponseEntity.ok(
                goalService.getGoal(userId, goalId)
        );
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<GoalResponse> updateGoal(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String goalId,
            @RequestBody GoalRequest request) {

        return ResponseEntity.ok(
                goalService.updateGoal(userId, goalId, request)
        );
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String goalId) {

        goalService.deleteGoal(userId, goalId);

        return ResponseEntity.noContent().build();
    }
}
