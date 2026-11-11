package com.fitness.activityservice.service;

import com.fitness.activityservice.WorkoutPlanRepository;
import com.fitness.activityservice.dto.WorkoutExerciseRequest;
import com.fitness.activityservice.dto.WorkoutExerciseResponse;
import com.fitness.activityservice.dto.WorkoutPlanRequest;
import com.fitness.activityservice.dto.WorkoutPlanResponse;
import com.fitness.activityservice.model.WorkoutExercise;
import com.fitness.activityservice.model.WorkoutPlan;
import com.fitness.activityservice.model.WorkoutPlanStatus;
import com.fitness.activityservice.dto.WorkoutAdherenceResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

        private final WorkoutPlanRepository workoutPlanRepository;

        // CREATE WORKOUT PLAN
        public WorkoutPlanResponse createWorkoutPlan(
                        WorkoutPlanRequest request,
                        String userId) {
                validateRequest(request);

                WorkoutPlan workoutPlan = WorkoutPlan.builder()
                                .userId(userId)
                                .title(request.getTitle().trim())
                                .description(request.getDescription())
                                .scheduledAt(request.getScheduledAt())
                                .status(WorkoutPlanStatus.PLANNED)
                                .exercises(mapExercises(request.getExercises()))
                                .build();

                WorkoutPlan savedPlan = workoutPlanRepository.save(workoutPlan);

                return mapToResponse(savedPlan);
        }

        // GET ALL WORKOUT PLANS FOR A USER
        public List<WorkoutPlanResponse> getUserWorkoutPlans(String userId) {

                return workoutPlanRepository
                                .findByUserIdOrderByScheduledAtAsc(userId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        // GET A SINGLE WORKOUT PLAN
        public WorkoutPlanResponse getWorkoutPlan(
                        String planId,
                        String userId) {
                WorkoutPlan workoutPlan = findUserPlan(planId, userId);

                return mapToResponse(workoutPlan);
        }

        // UPDATE WORKOUT PLAN
        public WorkoutPlanResponse updateWorkoutPlan(
                        String planId,
                        WorkoutPlanRequest request,
                        String userId) {
                validateRequest(request);

                WorkoutPlan workoutPlan = findUserPlan(planId, userId);

                if (workoutPlan.getStatus() != WorkoutPlanStatus.PLANNED) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Only planned workouts can be updated.");
                }

                workoutPlan.setTitle(request.getTitle().trim());
                workoutPlan.setDescription(request.getDescription());
                workoutPlan.setScheduledAt(request.getScheduledAt());
                workoutPlan.setExercises(mapExercises(request.getExercises()));

                WorkoutPlan updatedPlan = workoutPlanRepository.save(workoutPlan);

                return mapToResponse(updatedPlan);
        }

        // DELETE WORKOUT PLAN
        public void deleteWorkoutPlan(
                        String planId,
                        String userId) {
                WorkoutPlan workoutPlan = findUserPlan(planId, userId);

                if (workoutPlan.getStatus() == WorkoutPlanStatus.COMPLETED) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Completed workouts cannot be deleted.");
                }

                workoutPlanRepository.delete(workoutPlan);
        }

        // FIND PLAN BELONGING TO USER
        private WorkoutPlan findUserPlan(
                        String planId,
                        String userId) {
                return workoutPlanRepository
                                .findByIdAndUserId(planId, userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Workout plan not found."));
        }

        public WorkoutPlanResponse completeWorkoutPlan(String planId, String userId) {
                WorkoutPlan plan = workoutPlanRepository
                                .findByIdAndUserId(planId, userId)
                                .orElseThrow(() -> new RuntimeException("Workout plan not found"));

                if (plan.getStatus() == WorkoutPlanStatus.CANCELLED) {
                        throw new IllegalStateException(
                                        "A cancelled workout cannot be completed");
                }

                if (plan.getStatus() == WorkoutPlanStatus.COMPLETED) {
                        return mapToResponse(plan);
                }

                plan.setStatus(WorkoutPlanStatus.COMPLETED);
                plan.setCompletedAt(LocalDateTime.now());

                WorkoutPlan savedPlan = workoutPlanRepository.save(plan);

                return mapToResponse(savedPlan);
        }

        public List<WorkoutPlanResponse> getWorkoutHistory(String userId) {
                return workoutPlanRepository
                                .findByUserIdAndStatusOrderByScheduledAtAsc(
                                                userId,
                                                WorkoutPlanStatus.COMPLETED)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        // public WorkoutAdherenceResponse getWorkoutAdherence(
        // String userId,
        // int periodDays) {
        // if (periodDays < 1 || periodDays > 365) {
        // throw new IllegalArgumentException(
        // "Period must be between 1 and 365 days");
        // }

        // LocalDateTime now = LocalDateTime.now();
        // LocalDateTime startDate = now.minusDays(periodDays);

        // List<WorkoutPlan> periodPlans = workoutPlanRepository
        // .findByUserIdAndScheduledAtBetweenOrderByScheduledAtAsc(
        // userId,
        // startDate,
        // now);

        // long totalScheduled = periodPlans.stream()
        // .filter(plan -> plan.getStatus() != WorkoutPlanStatus.CANCELLED)
        // .count();

        // long completed = periodPlans.stream()
        // .filter(plan -> plan.getStatus() == WorkoutPlanStatus.COMPLETED)
        // .count();

        // long cancelled = periodPlans.stream()
        // .filter(plan -> plan.getStatus() == WorkoutPlanStatus.CANCELLED)
        // .count();

        // long pending = totalScheduled - completed;

        // double adherence = totalScheduled == 0
        // ? 0.0
        // : Math.round((completed * 10000.0) / totalScheduled) / 100.0;

        // return WorkoutAdherenceResponse.builder()
        // .periodDays(periodDays)
        // .totalScheduled(totalScheduled)
        // .completed(completed)
        // .pending(pending)
        // .cancelled(cancelled)
        // .adherencePercentage(adherence)
        // .build();
        // }

        public WorkoutAdherenceResponse getWorkoutAdherence(
                        String userId,
                        int periodDays) {

                if (periodDays < 1 || periodDays > 365) {
                        throw new IllegalArgumentException(
                                        "Period must be between 1 and 365 days");
                }

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = now.minusDays(periodDays);

                // Fetch all workout plans belonging to this user.
                List<WorkoutPlan> allPlans = workoutPlanRepository
                                .findByUserIdOrderByScheduledAtAsc(userId);

                // Include workouts scheduled during the period.
                // Also include workouts completed during the period,
                // even if their scheduled date is in the future.
                List<WorkoutPlan> periodPlans = allPlans.stream()
                                .filter(plan -> {

                                        boolean scheduledWithinPeriod = plan.getScheduledAt() != null
                                                        && !plan.getScheduledAt().isBefore(startDate)
                                                        && !plan.getScheduledAt().isAfter(now);

                                        boolean completedWithinPeriod = plan.getStatus() == WorkoutPlanStatus.COMPLETED
                                                        && plan.getCompletedAt() != null
                                                        && !plan.getCompletedAt().isBefore(startDate)
                                                        && !plan.getCompletedAt().isAfter(now);

                                        return scheduledWithinPeriod
                                                        || completedWithinPeriod;
                                })
                                .toList();

                long totalScheduled = periodPlans.stream()
                                .filter(plan -> plan.getStatus() != WorkoutPlanStatus.CANCELLED)
                                .count();

                long completed = periodPlans.stream()
                                .filter(plan -> plan.getStatus() == WorkoutPlanStatus.COMPLETED)
                                .count();

                long cancelled = periodPlans.stream()
                                .filter(plan -> plan.getStatus() == WorkoutPlanStatus.CANCELLED)
                                .count();

                long pending = totalScheduled - completed;

                double adherence = totalScheduled == 0
                                ? 0.0
                                : Math.round(
                                                (completed * 10000.0) / totalScheduled) / 100.0;

                return WorkoutAdherenceResponse.builder()
                                .periodDays(periodDays)
                                .totalScheduled(totalScheduled)
                                .completed(completed)
                                .pending(pending)
                                .cancelled(cancelled)
                                .adherencePercentage(adherence)
                                .build();
        }

        // VALIDATE REQUEST
        private void validateRequest(WorkoutPlanRequest request) {

                if (request == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Workout plan request cannot be empty.");
                }

                if (request.getTitle() == null ||
                                request.getTitle().isBlank()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Workout title is required.");
                }

                if (request.getTitle().length() > 100) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Workout title cannot exceed 100 characters.");
                }

                if (request.getScheduledAt() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Workout schedule date and time are required.");
                }

                if (request.getScheduledAt().isBefore(LocalDateTime.now())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Workout cannot be scheduled in the past.");
                }

                if (request.getExercises() == null ||
                                request.getExercises().isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Add at least one exercise.");
                }

                for (WorkoutExerciseRequest exercise : request.getExercises()) {

                        if (exercise == null ||
                                        exercise.getExerciseName() == null ||
                                        exercise.getExerciseName().isBlank()) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Every exercise must have a name.");
                        }

                        if (exercise.getSets() != null && exercise.getSets() <= 0) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Exercise sets must be greater than zero.");
                        }

                        if (exercise.getReps() != null && exercise.getReps() <= 0) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Exercise reps must be greater than zero.");
                        }

                        if (exercise.getDurationMinutes() != null &&
                                        exercise.getDurationMinutes() <= 0) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Exercise duration must be greater than zero.");
                        }

                        if (exercise.getSets() == null &&
                                        exercise.getReps() == null &&
                                        exercise.getDurationMinutes() == null) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Provide sets and reps or a duration for each exercise.");
                        }
                }
        }

        // MAP REQUEST EXERCISES TO MODEL
        private List<WorkoutExercise> mapExercises(
                        List<WorkoutExerciseRequest> exercises) {
                if (exercises == null) {
                        return Collections.emptyList();
                }

                return exercises.stream()
                                .map(exercise -> WorkoutExercise.builder()
                                                .exerciseName(exercise.getExerciseName().trim())
                                                .sets(exercise.getSets())
                                                .reps(exercise.getReps())
                                                .durationMinutes(exercise.getDurationMinutes())
                                                .notes(exercise.getNotes())
                                                .build())
                                .toList();
        }

        // MAP MODEL TO RESPONSE
        private WorkoutPlanResponse mapToResponse(WorkoutPlan workoutPlan) {

                List<WorkoutExerciseResponse> exercises = workoutPlan.getExercises() == null
                                ? Collections.emptyList()
                                : workoutPlan.getExercises().stream()
                                                .map(exercise -> WorkoutExerciseResponse.builder()
                                                                .exerciseName(exercise.getExerciseName())
                                                                .sets(exercise.getSets())
                                                                .reps(exercise.getReps())
                                                                .durationMinutes(exercise.getDurationMinutes())
                                                                .notes(exercise.getNotes())
                                                                .build())
                                                .toList();

                return WorkoutPlanResponse.builder()
                                .id(workoutPlan.getId())
                                .title(workoutPlan.getTitle())
                                .description(workoutPlan.getDescription())
                                .scheduledAt(workoutPlan.getScheduledAt())
                                .status(workoutPlan.getStatus())
                                .exercises(exercises)
                                .completedActivityId(workoutPlan.getCompletedActivityId())
                                .completedAt(workoutPlan.getCompletedAt())
                                .createdAt(workoutPlan.getCreatedAt())
                                .updatedAt(workoutPlan.getUpdatedAt())
                                .build();
        }
}