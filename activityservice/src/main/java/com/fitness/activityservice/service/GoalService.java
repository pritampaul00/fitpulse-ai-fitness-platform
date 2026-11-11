package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.GoalRepository;
import com.fitness.activityservice.dto.GoalRequest;
import com.fitness.activityservice.dto.GoalResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.Goal;
import com.fitness.activityservice.model.GoalStatus;
import com.fitness.activityservice.model.GoalType;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final ActivityRepository activityRepository;

    public GoalResponse createGoal(String userId, GoalRequest request) {

        validateRequest(request);

        Goal goal = Goal.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .goalType(request.getGoalType())
                .targetValue(request.getTargetValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(GoalStatus.ACTIVE)
                .build();

        return buildResponse(goalRepository.save(goal));
    }

    public List<GoalResponse> getUserGoals(String userId) {

        return goalRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public GoalResponse getGoal(String userId, String goalId) {

        return buildResponse(findGoal(userId, goalId));
    }

    public GoalResponse updateGoal(
            String userId,
            String goalId,
            GoalRequest request) {

        validateRequest(request);

        Goal goal = findGoal(userId, goalId);

        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription());
        goal.setGoalType(request.getGoalType());
        goal.setTargetValue(request.getTargetValue());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());

        if (goal.getStatus() != GoalStatus.COMPLETED) {
            goal.setStatus(GoalStatus.ACTIVE);
        }

        return buildResponse(goalRepository.save(goal));
    }

    public void deleteGoal(String userId, String goalId) {

        Goal goal = findGoal(userId, goalId);
        goalRepository.delete(goal);
    }

    private Goal findGoal(String userId, String goalId) {

        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Goal not found"));
    }

    private void validateRequest(GoalRequest request) {

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Goal title is required");
        }

        if (request.getTitle().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Goal title cannot exceed 100 characters");
        }

        if (request.getGoalType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Goal type is required");
        }

        if (request.getTargetValue() == null ||
                !Double.isFinite(request.getTargetValue()) ||
                request.getTargetValue() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target must be a positive number");
        }

        if (request.getStartDate() == null ||
                request.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date");
        }

        if (request.getDescription() != null &&
                request.getDescription().length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description cannot exceed 500 characters");
        }
    }

    private GoalResponse buildResponse(Goal goal) {

        List<Activity> activities =
                activityRepository.findByUserId(goal.getUserId());

        double currentValue = calculateProgress(goal, activities);

        double percentage = Math.min(
                100.0,
                (currentValue / goal.getTargetValue()) * 100.0
        );

        GoalStatus status = goal.getStatus();

        if (currentValue >= goal.getTargetValue()) {
            status = GoalStatus.COMPLETED;
        } else if (goal.getEndDate().isBefore(LocalDate.now())) {
            status = GoalStatus.EXPIRED;
        } else {
            status = GoalStatus.ACTIVE;
        }

        if (status != goal.getStatus()) {
            goal.setStatus(status);
            goalRepository.save(goal);
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .goalType(goal.getGoalType())
                .targetValue(goal.getTargetValue())
                .currentValue(currentValue)
                .progressPercentage(
                        Math.round(percentage * 100.0) / 100.0
                )
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .status(status)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    private double calculateProgress(
            Goal goal,
            List<Activity> activities) {

        LocalDateTime start = goal.getStartDate().atStartOfDay();

        LocalDateTime end = goal.getEndDate()
                .plusDays(1)
                .atStartOfDay();

        return activities.stream()
                .filter(activity -> {
                    LocalDateTime activityDate =
                            activity.getStartTime() != null
                                    ? activity.getStartTime()
                                    : activity.getCreatedAt();

                    return activityDate != null
                            && !activityDate.isBefore(start)
                            && activityDate.isBefore(end);
                })
                .mapToDouble(activity -> getActivityValue(
                        activity,
                        goal.getGoalType()
                ))
                .sum();
    }

    private double getActivityValue(
            Activity activity,
            GoalType goalType) {

        return switch (goalType) {
            case WORKOUT_COUNT -> 1.0;

            case DURATION_MINUTES ->
                    activity.getDuration() == null
                            ? 0.0
                            : activity.getDuration();

            case CALORIES ->
                    activity.getCaloriesBurned() == null
                            ? 0.0
                            : activity.getCaloriesBurned();
        };
    }
}
