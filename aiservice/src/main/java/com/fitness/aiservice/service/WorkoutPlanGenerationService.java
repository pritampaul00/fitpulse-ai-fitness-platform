package com.fitness.aiservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fitness.aiservice.dto.ActivityInsightItem;
import com.fitness.aiservice.dto.GoalInsightItem;
import com.fitness.aiservice.dto.WorkoutPlanContextItem;
import com.fitness.aiservice.dto.WorkoutPlanGenerationRequest;
import com.fitness.aiservice.dto.WorkoutPlanGenerationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutPlanGenerationService {

        private final GeminiService geminiService;
        private final GoalInsightClient goalInsightClient;
        private final ObjectMapper objectMapper;

        public WorkoutPlanGenerationResponse generateWorkoutPlan(
                        String userId,
                        WorkoutPlanGenerationRequest request) {

                validateRequest(request);

                log.info("Generating AI workout plan for user: {}", userId);

                List<GoalInsightItem> goals = safeList(goalInsightClient.getGoals(userId));

                List<ActivityInsightItem> activities = safeList(goalInsightClient.getActivities(userId));

                List<WorkoutPlanContextItem> existingPlans = safeList(goalInsightClient.getWorkoutPlans(userId));

                String context = buildFitnessContext(
                                goals,
                                activities,
                                existingPlans);

                String prompt = buildGenerationPrompt(request, context);

                List<Map<String, String>> messages = new ArrayList<>();

                messages.add(Map.of(
                                "role", "system",
                                "content",
                                """
                                                You are an experienced fitness coach and workout planner.

                                                Generate safe, realistic, personalized workout plans.

                                                Follow the user's fitness level, goals, equipment,
                                                available training days, and session duration.

                                                Use the supplied fitness context when relevant.

                                                Never invent user statistics or claim that the user
                                                has completed activities that are not in the context.

                                                OUTPUT RULES:
                                                1. Return one complete JSON object.
                                                2. Do not use Markdown code fences.
                                                3. Do not include explanations outside the JSON.
                                                4. Never stop in the middle of a JSON object or array.
                                                5. Keep exercise notes concise, preferably under 20 words.
                                                6. Generate 4 to 6 exercises per session.
                                                7. Use null for fields that do not apply.
                                                8. Ensure every JSON string and array is properly closed.
                                                9. Return valid JSON that can be parsed directly.

                                                If you cannot fit the complete plan in the response,
                                                prioritize completing the JSON structure.
                                                """));

                messages.add(Map.of(
                                "role", "user",
                                "content", prompt));

                // String aiResponse = geminiService.getChatAnswer(messages);
                String aiResponse = geminiService.getWorkoutPlanAnswer(messages);

                return parseAiResponse(aiResponse);
        }

        private void validateRequest(WorkoutPlanGenerationRequest request) {

                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Workout generation request cannot be empty.");
                }

                if (request.getDaysPerWeek() == null
                                || request.getDaysPerWeek() < 1
                                || request.getDaysPerWeek() > 7) {

                        throw new IllegalArgumentException(
                                        "Days per week must be between 1 and 7.");
                }

                if (request.getSessionDurationMinutes() == null
                                || request.getSessionDurationMinutes() < 15
                                || request.getSessionDurationMinutes() > 180) {

                        throw new IllegalArgumentException(
                                        "Session duration must be between 15 and 180 minutes.");
                }

                if (request.getFitnessLevel() == null
                                || request.getFitnessLevel().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Fitness level is required.");
                }

                if (request.getAvailableDays() == null
                                || request.getAvailableDays().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Please select your available training days.");
                }

                if (request.getAvailableDays().size() != request.getDaysPerWeek()) {
                        throw new IllegalArgumentException(
                                        "Selected training days must match days per week.");
                }

                for (String day : request.getAvailableDays()) {
                        try {
                                java.time.DayOfWeek.valueOf(day.toUpperCase());
                        } catch (Exception exception) {
                                throw new IllegalArgumentException(
                                                "Invalid training day: " + day);
                        }
                }

                if (request.getStartDate() != null
                                && request.getStartDate().isBefore(LocalDate.now())) {

                        throw new IllegalArgumentException(
                                        "Start date cannot be in the past.");
                }
        }

        private String buildFitnessContext(
                        List<GoalInsightItem> goals,
                        List<ActivityInsightItem> activities,
                        List<WorkoutPlanContextItem> existingPlans) {

                StringBuilder context = new StringBuilder();

                context.append("USER FITNESS GOALS:\n");

                if (goals.isEmpty()) {
                        context.append("No fitness goals available.\n");
                } else {
                        goals.forEach(goal -> context.append(
                                        String.format(
                                                        "- Goal: %s, Type: %s, Target: %s, Current: %s, Progress: %s%%, Status: %s%n",
                                                        goal.getTitle(),
                                                        goal.getGoalType(),
                                                        goal.getTargetValue(),
                                                        goal.getCurrentValue(),
                                                        goal.getProgressPercentage(),
                                                        goal.getStatus())));
                }

                context.append("\nRECENT ACTIVITY HISTORY:\n");

                if (activities.isEmpty()) {
                        context.append("No activity history available.\n");
                } else {
                        activities.stream()
                                        .filter(Objects::nonNull)
                                        .limit(20)
                                        .forEach(activity -> context.append(
                                                        String.format(
                                                                        "- Type: %s, Duration: %s minutes, Calories: %s, Date: %s%n",
                                                                        activity.getType(),
                                                                        activity.getDuration(),
                                                                        activity.getCaloriesBurned(),
                                                                        activity.getStartTime() != null
                                                                                        ? activity.getStartTime()
                                                                                        : activity.getCreatedAt())));
                }

                context.append("\nEXISTING WORKOUT PLANS:\n");

                if (existingPlans.isEmpty()) {
                        context.append("No existing workout plans available.\n");
                } else {
                        existingPlans.stream()
                                        .filter(Objects::nonNull)
                                        .limit(30)
                                        .forEach(plan -> {
                                                context.append(
                                                                String.format(
                                                                                "- Title: %s, Scheduled: %s, Status: %s%n",
                                                                                plan.getTitle(),
                                                                                plan.getScheduledAt(),
                                                                                plan.getStatus()));

                                                if (plan.getExercises() != null) {
                                                        plan.getExercises().forEach(exercise -> context.append(
                                                                        String.format(
                                                                                        "  Exercise: %s, Sets: %s, Reps: %s, Duration: %s minutes%n",
                                                                                        exercise.getExerciseName(),
                                                                                        exercise.getSets(),
                                                                                        exercise.getReps(),
                                                                                        exercise.getDurationMinutes())));
                                                }
                                        });
                }

                return context.toString();
        }

        private String buildGenerationPrompt(
                        WorkoutPlanGenerationRequest request,
                        String fitnessContext) {

                LocalDate startDate = request.getStartDate() != null
                                ? request.getStartDate()
                                : LocalDate.now().plusDays(1);

                LocalTime startTime = request.getPreferredStartTime() != null
                                ? request.getPreferredStartTime()
                                : LocalTime.of(7, 0);

                String equipment = request.getEquipment() == null
                                || request.getEquipment().isEmpty()
                                                ? "No equipment specified. Prefer bodyweight exercises."
                                                : String.join(", ", request.getEquipment());

                String preferences = request.getPreferences() == null
                                || request.getPreferences().isBlank()
                                                ? "No additional preferences."
                                                : request.getPreferences();

                String focus = request.getWorkoutFocus() == null
                                || request.getWorkoutFocus().isBlank()
                                                ? "General fitness"
                                                : request.getWorkoutFocus();

                return """
                                Generate a personalized weekly workout plan.

                                USER TRAINING PREFERENCES:

                                Fitness level: %s

                                Workout focus: %s

                                Training sessions per week: %d

                                Session duration: %d minutes

                                Available equipment: %s

                                Available training days: %s

                                Start date: %s

                                Preferred workout time: %s

                                Additional preferences: %s

                                USER FITNESS CONTEXT:

                                %s

                                GENERATION REQUIREMENTS:

                                1. Generate exactly %d workout sessions.
                                2. Schedule sessions only on the selected available days.
                                3. Start scheduling on or after the specified start date.
                                4. Use the preferred workout time.
                                5. Do not schedule duplicate sessions on the same date.
                                6. Consider existing workout plans and avoid scheduling conflicts.
                                7. Respect the user's equipment limitations.
                                8. Match exercise difficulty to the user's fitness level.
                                9. Consider the user's fitness goals and activity history.
                                10. Include warm-up and recovery considerations in exercise notes.
                                11. Avoid excessive training volume and consecutive intense sessions.
                                12. Every exercise must have an exerciseName.
                                13. Every exercise must have at least one positive value among sets, reps, or durationMinutes.
                                14. Use null for fields that do not apply.
                                15. Keep session duration close to the requested duration.
                                16. Do not prescribe exercises that require unavailable equipment.

                                RESPONSE FORMAT:

                                Return a JSON object with this exact structure:

                                {
                                  "summary": "Short overview of the weekly workout plan",
                                  "sessions": [
                                    {
                                      "title": "Workout title",
                                      "description": "Workout description",
                                      "scheduledAt": "YYYY-MM-DDTHH:mm:ss",
                                      "rationale": "Why this workout was selected",
                                      "exercises": [
                                        {
                                          "exerciseName": "Exercise name",
                                          "sets": 3,
                                          "reps": 12,
                                          "durationMinutes": null,
                                          "notes": "Exercise instructions"
                                        }
                                      ]
                                    }
                                  ]
                                }

                                Return exactly %d sessions.
                                Return only JSON.
                                """
                                .formatted(
                                                request.getFitnessLevel(),
                                                focus,
                                                request.getDaysPerWeek(),
                                                request.getSessionDurationMinutes(),
                                                equipment,
                                                String.join(", ", request.getAvailableDays()),
                                                startDate,
                                                startTime,
                                                preferences,
                                                fitnessContext,
                                                request.getDaysPerWeek(),
                                                request.getDaysPerWeek());
        }

        private WorkoutPlanGenerationResponse parseAiResponse(String aiResponse) {

                if (aiResponse == null || aiResponse.isBlank()) {
                        throw new IllegalStateException(
                                        "AI returned an empty workout plan.");
                }

                try {
                        String json = extractJson(aiResponse);

                        WorkoutPlanGenerationResponse response = objectMapper.readValue(
                                        json,
                                        WorkoutPlanGenerationResponse.class);

                        validateGeneratedResponse(response);

                        return response;

                } catch (JsonProcessingException exception) {

                        log.error("Failed to parse AI workout response. Response length: {} characters. Error: {}",
                                        aiResponse.length(), exception.getOriginalMessage());

                        throw new IllegalStateException(
                                        "AI returned an invalid workout plan. Please try again.",
                                        exception);
                }
        }

        private String extractJson(String response) {

                String cleaned = response.trim();

                if (cleaned.startsWith("```")) {
                        cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
                        cleaned = cleaned.replaceFirst("\\s*```$", "");
                }

                int firstBrace = cleaned.indexOf('{');
                int lastBrace = cleaned.lastIndexOf('}');

                if (firstBrace == -1 || lastBrace == -1
                                || lastBrace <= firstBrace) {

                        throw new IllegalStateException(
                                        "AI response does not contain valid JSON.");
                }

                return cleaned.substring(firstBrace, lastBrace + 1);
        }

        private void validateGeneratedResponse(
                        WorkoutPlanGenerationResponse response) {

                if (response == null
                                || response.getSessions() == null
                                || response.getSessions().isEmpty()) {

                        throw new IllegalStateException(
                                        "AI did not generate any workout sessions.");
                }

                for (WorkoutPlanGenerationResponse.GeneratedWorkoutSession session : response.getSessions()) {

                        if (session.getTitle() == null
                                        || session.getTitle().isBlank()) {

                                throw new IllegalStateException(
                                                "Generated workout is missing a title.");
                        }

                        if (session.getScheduledAt() == null) {
                                throw new IllegalStateException(
                                                "Generated workout is missing its scheduled date.");
                        }

                        if (session.getExercises() == null
                                        || session.getExercises().isEmpty()) {

                                throw new IllegalStateException(
                                                "Generated workout has no exercises.");
                        }

                        for (WorkoutPlanGenerationResponse.GeneratedWorkoutExercise exercise : session.getExercises()) {

                                if (exercise.getExerciseName() == null
                                                || exercise.getExerciseName().isBlank()) {

                                        throw new IllegalStateException(
                                                        "Generated exercise is missing its name.");
                                }

                                boolean hasValidPrescription = isPositive(exercise.getSets())
                                                || isPositive(exercise.getReps())
                                                || isPositive(exercise.getDurationMinutes());

                                if (!hasValidPrescription) {
                                        throw new IllegalStateException(
                                                        "Generated exercise must include sets, reps, or duration.");
                                }
                        }
                }
        }

        private boolean isPositive(Integer value) {
                return value != null && value > 0;
        }

        private <T> List<T> safeList(List<T> items) {
                return items == null ? List.of() : items;
        }
}