package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.WorkoutPlanRequest;
import com.fitness.activityservice.dto.WorkoutPlanResponse;
import com.fitness.activityservice.dto.WorkoutAdherenceResponse;
import com.fitness.activityservice.service.WorkoutPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

        private final WorkoutPlanService workoutPlanService;

        // CREATE WORKOUT PLAN
        @PostMapping
        public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
                        @RequestBody WorkoutPlanRequest request,
                        @RequestHeader("X-User-ID") String userId) {

                WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(request, userId);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // GET ALL WORKOUT PLANS
        @GetMapping
        public ResponseEntity<List<WorkoutPlanResponse>> getUserWorkoutPlans(
                        @RequestHeader("X-User-ID") String userId) {

                return ResponseEntity.ok(
                                workoutPlanService.getUserWorkoutPlans(userId));
        }

        // GET WORKOUT PLAN BY ID
        @GetMapping("/{planId}")
        public ResponseEntity<WorkoutPlanResponse> getWorkoutPlan(
                        @PathVariable String planId,
                        @RequestHeader("X-User-ID") String userId) {

                return ResponseEntity.ok(
                                workoutPlanService.getWorkoutPlan(planId, userId));
        }

        // UPDATE WORKOUT PLAN
        @PutMapping("/{planId}")
        public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(
                        @PathVariable String planId,
                        @RequestBody WorkoutPlanRequest request,
                        @RequestHeader("X-User-ID") String userId) {

                return ResponseEntity.ok(
                                workoutPlanService.updateWorkoutPlan(
                                                planId,
                                                request,
                                                userId));
        }

        // DELETE WORKOUT PLAN
        @DeleteMapping("/{planId}")
        public ResponseEntity<Void> deleteWorkoutPlan(
                        @PathVariable String planId,
                        @RequestHeader("X-User-ID") String userId) {

                workoutPlanService.deleteWorkoutPlan(planId, userId);

                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{planId}/complete")
        public ResponseEntity<WorkoutPlanResponse> completeWorkoutPlan(
                        @PathVariable String planId,
                        @RequestHeader("X-User-ID") String userId) {

                return ResponseEntity.ok(
                                workoutPlanService.completeWorkoutPlan(planId, userId));
        }

        @GetMapping("/history")
        public ResponseEntity<List<WorkoutPlanResponse>> getWorkoutHistory(
                        @RequestHeader("X-User-ID") String userId) {

                return ResponseEntity.ok(
                                workoutPlanService.getWorkoutHistory(userId));
        }

        @GetMapping("/adherence")
        public ResponseEntity<WorkoutAdherenceResponse> getWorkoutAdherence(
                        @RequestHeader("X-User-ID") String userId,
                        @RequestParam(defaultValue = "30") int days) {

                return ResponseEntity.ok(
                                workoutPlanService.getWorkoutAdherence(userId, days));
        }
}