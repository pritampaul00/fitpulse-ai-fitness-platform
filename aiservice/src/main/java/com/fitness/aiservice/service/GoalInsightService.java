package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.ActivityInsightItem;
import com.fitness.aiservice.dto.GoalInsightItem;
import com.fitness.aiservice.dto.GoalInsightResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalInsightService {

    private final GoalInsightClient goalInsightClient;
    private final GeminiService geminiService;

    public GoalInsightResponse generateInsights(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        List<GoalInsightItem> goals =
                goalInsightClient.getGoals(userId);

        List<ActivityInsightItem> activities =
                goalInsightClient.getActivities(userId);

        if (goals == null) {
            goals = List.of();
        }

        if (activities == null) {
            activities = List.of();
        }

        String context = buildContext(goals, activities);

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", """
                        You are FitPulse AI, a practical and responsible fitness goal coach.

                        Analyze only the user's supplied goals and recorded activities.

                        Rules:
                        1. Never invent workouts, calories, duration, dates, or progress.
                        2. Use the supplied goal progress values as the source of truth.
                        3. Clearly distinguish recorded facts from estimates.
                        4. Assess whether each active goal appears on track.
                        5. Consider the goal deadline and remaining target.
                        6. Give realistic recommendations based on recorded activity.
                        7. If there is insufficient history, say so.
                        8. Do not diagnose medical conditions or recommend unsafe training.
                        9. Do not guarantee that a goal will be completed.
                        10. Keep the response useful, specific, and easy to read.

                        Format your response using Markdown with these sections:

                        ## Overall assessment
                        ## Goal-by-goal analysis
                        ## What you should focus on
                        ## Your next 7-day action plan
                        ## Final recommendation

                        If the user has no goals, explain that they should create a goal first.
                        """
        ));

        messages.add(Map.of(
                "role", "user",
                "content", context
        ));

        String answer = geminiService.getChatAnswer(messages);

        return GoalInsightResponse.builder()
                .insights(answer)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private String buildContext(
            List<GoalInsightItem> goals,
            List<ActivityInsightItem> activities) {

        LocalDate today = LocalDate.now();

        StringBuilder context = new StringBuilder();

        context.append("Today's date: ")
                .append(today)
                .append("\n\n");

        context.append("USER GOALS\n");

        if (goals.isEmpty()) {
            context.append("No goals have been created.\n");
        } else {
            for (GoalInsightItem goal : goals) {
                long daysRemaining = goal.getEndDate() == null
                        ? -1
                        : ChronoUnit.DAYS.between(
                                today,
                                goal.getEndDate()
                        );

                context.append("\nGoal: ")
                        .append(goal.getTitle())
                        .append("\nType: ")
                        .append(goal.getGoalType())
                        .append("\nTarget: ")
                        .append(goal.getTargetValue())
                        .append("\nCurrent value: ")
                        .append(goal.getCurrentValue())
                        .append("\nProgress: ")
                        .append(goal.getProgressPercentage())
                        .append("%\nStatus: ")
                        .append(goal.getStatus())
                        .append("\nStart date: ")
                        .append(goal.getStartDate())
                        .append("\nDeadline: ")
                        .append(goal.getEndDate())
                        .append("\nDays remaining: ")
                        .append(daysRemaining)
                        .append("\n");
            }
        }

        context.append("\nRECORDED ACTIVITIES\n");

        if (activities.isEmpty()) {
            context.append("No recorded activities found.\n");
        } else {
            int totalWorkouts = activities.size();

            int totalDuration = activities.stream()
                    .map(ActivityInsightItem::getDuration)
                    .filter(value -> value != null)
                    .mapToInt(Integer::intValue)
                    .sum();

            int totalCalories = activities.stream()
                    .map(ActivityInsightItem::getCaloriesBurned)
                    .filter(value -> value != null)
                    .mapToInt(Integer::intValue)
                    .sum();

            context.append("Total recorded workouts: ")
                    .append(totalWorkouts)
                    .append("\nTotal recorded duration: ")
                    .append(totalDuration)
                    .append(" minutes\nTotal recorded calories: ")
                    .append(totalCalories)
                    .append("\n");

            context.append("\nRecent activities:\n");

            activities.stream()
                    .sorted((first, second) ->
                            getActivityDate(second).compareTo(
                                    getActivityDate(first)
                            ))
                    .limit(20)
                    .forEach(activity -> context
                            .append("- Type: ")
                            .append(activity.getType())
                            .append(", Duration: ")
                            .append(activity.getDuration())
                            .append(" minutes, Calories: ")
                            .append(activity.getCaloriesBurned())
                            .append(", Date: ")
                            .append(getActivityDate(activity))
                            .append("\n"));
        }

        return context.toString();
    }

    private LocalDateTime getActivityDate(
            ActivityInsightItem activity) {

        if (activity.getStartTime() != null) {
            return activity.getStartTime();
        }

        if (activity.getCreatedAt() != null) {
            return activity.getCreatedAt();
        }

        return LocalDateTime.MIN;
    }
}
