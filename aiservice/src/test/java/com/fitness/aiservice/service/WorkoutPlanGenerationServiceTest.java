package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.dto.WorkoutPlanGenerationRequest;
import com.fitness.aiservice.dto.WorkoutPlanGenerationResponse;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanGenerationServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private GoalInsightClient goalInsightClient;

    private WorkoutPlanGenerationService service;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        service = new WorkoutPlanGenerationService(
                geminiService,
                goalInsightClient,
                objectMapper);
    }

    private WorkoutPlanGenerationRequest validRequest() {
        WorkoutPlanGenerationRequest request = new WorkoutPlanGenerationRequest();

        request.setDaysPerWeek(3);
        request.setSessionDurationMinutes(45);
        request.setFitnessLevel("BEGINNER");
        request.setEquipment(List.of("DUMBBELLS"));
        request.setAvailableDays(
                List.of("MONDAY", "WEDNESDAY", "FRIDAY"));
        request.setPreferredStartTime(LocalTime.of(7, 0));
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setWorkoutFocus("GENERAL_FITNESS");
        request.setPreferences("Keep workouts beginner friendly");

        return request;
    }

    private String validAiResponse() {
        return """
                {
                  "summary": "A beginner-friendly weekly plan",
                  "sessions": [
                    {
                      "title": "Full Body Workout",
                      "description": "A balanced full body session",
                      "scheduledAt": "2026-11-13T07:00:00",
                      "rationale": "Build a consistent fitness routine",
                      "exercises": [
                        {
                          "exerciseName": "Squats",
                          "sets": 3,
                          "reps": 12,
                          "durationMinutes": null,
                          "notes": "Use controlled movement"
                        },
                        {
                          "exerciseName": "Push Ups",
                          "sets": 3,
                          "reps": 10,
                          "durationMinutes": null,
                          "notes": "Maintain proper form"
                        }
                      ]
                    }
                  ]
                }
                """;
    }

    private void mockFitnessContext() {
        when(goalInsightClient.getGoals(USER_ID))
                .thenReturn(List.of());

        when(goalInsightClient.getActivities(USER_ID))
                .thenReturn(List.of());

        when(goalInsightClient.getWorkoutPlans(USER_ID))
                .thenReturn(List.of());
    }

    @Test
    void generateWorkoutPlanShouldReturnValidPlan() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn(validAiResponse());

        WorkoutPlanGenerationResponse response = service.generateWorkoutPlan(USER_ID, validRequest());

        assertNotNull(response);
        assertEquals(
                "A beginner-friendly weekly plan",
                response.getSummary());
        assertEquals(1, response.getSessions().size());

        WorkoutPlanGenerationResponse.GeneratedWorkoutSession session = response.getSessions().get(0);

        assertEquals("Full Body Workout", session.getTitle());
        assertEquals(2, session.getExercises().size());
        assertEquals(
                "Squats",
                session.getExercises().get(0).getExerciseName());

        verify(goalInsightClient).getGoals(USER_ID);
        verify(goalInsightClient).getActivities(USER_ID);
        verify(goalInsightClient).getWorkoutPlans(USER_ID);
        verify(geminiService).getWorkoutPlanAnswer(anyList());
    }

    @Test
    void generateWorkoutPlanShouldParseMarkdownWrappedJson() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn("```json\n" + validAiResponse() + "\n```");

        WorkoutPlanGenerationResponse response = service.generateWorkoutPlan(USER_ID, validRequest());

        assertNotNull(response);
        assertEquals(1, response.getSessions().size());
    }

    @Test
    void generateWorkoutPlanShouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, null));

        verifyNoInteractions(geminiService);
        verifyNoInteractions(goalInsightClient);
    }

    @Test
    void generateWorkoutPlanShouldRejectInvalidDaysPerWeek() {
        WorkoutPlanGenerationRequest request = validRequest();
        request.setDaysPerWeek(8);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, request));

        verifyNoInteractions(geminiService);
        verifyNoInteractions(goalInsightClient);
    }

    @Test
    void generateWorkoutPlanShouldRejectInvalidSessionDuration() {
        WorkoutPlanGenerationRequest request = validRequest();
        request.setSessionDurationMinutes(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, request));

        verifyNoInteractions(geminiService);
    }

    @Test
    void generateWorkoutPlanShouldRejectMissingFitnessLevel() {
        WorkoutPlanGenerationRequest request = validRequest();
        request.setFitnessLevel(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, request));

        verifyNoInteractions(geminiService);
    }

    @Test
    void generateWorkoutPlanShouldRejectTrainingDayCountMismatch() {
        WorkoutPlanGenerationRequest request = validRequest();
        request.setAvailableDays(List.of("MONDAY", "FRIDAY"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, request));

        verifyNoInteractions(geminiService);
    }

    @Test
    void generateWorkoutPlanShouldRejectInvalidTrainingDay() {
        WorkoutPlanGenerationRequest request = validRequest();
        request.setAvailableDays(
                List.of("MONDAY", "WEDNESDAY", "FUNDAY"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, request));

        verifyNoInteractions(geminiService);
    }

    @Test
    void generateWorkoutPlanShouldRejectPastStartDate() {
        WorkoutPlanGenerationRequest request = validRequest();
        request.setStartDate(LocalDate.now().minusDays(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateWorkoutPlan(USER_ID, request));

        verifyNoInteractions(geminiService);
    }

    @Test
    void generateWorkoutPlanShouldRejectEmptyAiResponse() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn(" ");

        assertThrows(
                IllegalStateException.class,
                () -> service.generateWorkoutPlan(USER_ID, validRequest()));
    }

    @Test
    void generateWorkoutPlanShouldRejectInvalidJson() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn("This is not valid JSON");

        assertThrows(
                IllegalStateException.class,
                () -> service.generateWorkoutPlan(USER_ID, validRequest()));
    }

    @Test
    void generateWorkoutPlanShouldRejectResponseWithoutSessions() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn("""
                        {
                          "summary": "Empty plan",
                          "sessions": []
                        }
                        """);

        assertThrows(
                IllegalStateException.class,
                () -> service.generateWorkoutPlan(USER_ID, validRequest()));
    }

    @Test
    void generateWorkoutPlanShouldRejectSessionWithoutTitle() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn("""
                        {
                          "summary": "Plan",
                          "sessions": [
                            {
                              "title": " ",
                              "scheduledAt": "2026-11-13T07:00:00",
                              "exercises": [
                                {
                                  "exerciseName": "Squats",
                                  "sets": 3
                                }
                              ]
                            }
                          ]
                        }
                        """);

        assertThrows(
                IllegalStateException.class,
                () -> service.generateWorkoutPlan(USER_ID, validRequest()));
    }

    @Test
    void generateWorkoutPlanShouldRejectExerciseWithoutPrescription() {
        mockFitnessContext();

        when(geminiService.getWorkoutPlanAnswer(anyList()))
                .thenReturn("""
                        {
                          "summary": "Plan",
                          "sessions": [
                            {
                              "title": "Workout",
                              "scheduledAt": "2026-11-13T07:00:00",
                              "exercises": [
                                {
                                  "exerciseName": "Squats",
                                  "sets": 0,
                                  "reps": 0,
                                  "durationMinutes": 0
                                }
                              ]
                            }
                          ]
                        }
                        """);

        assertThrows(
                IllegalStateException.class,
                () -> service.generateWorkoutPlan(USER_ID, validRequest()));
    }
}
