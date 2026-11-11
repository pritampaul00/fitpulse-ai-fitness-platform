package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.ActivityInsightItem;
import com.fitness.aiservice.dto.GoalInsightItem;
import com.fitness.aiservice.dto.GoalInsightResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalInsightServiceTest {

    @Mock
    private GoalInsightClient goalInsightClient;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private GoalInsightService goalInsightService;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        // Dependencies are injected by Mockito.
    }

    @Test
    void generateInsightsShouldReturnAiResponse() {
        when(goalInsightClient.getGoals(USER_ID))
                .thenReturn(List.of());

        when(goalInsightClient.getActivities(USER_ID))
                .thenReturn(List.of());

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn("Your fitness progress looks good.");

        GoalInsightResponse response = goalInsightService.generateInsights(USER_ID);

        assertNotNull(response);
        assertEquals(
                "Your fitness progress looks good.",
                response.getInsights());
        assertNotNull(response.getGeneratedAt());

        verify(goalInsightClient).getGoals(USER_ID);
        verify(goalInsightClient).getActivities(USER_ID);
        verify(geminiService).getChatAnswer(anyList());
    }

    @Test
    void generateInsightsShouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> goalInsightService.generateInsights(null));

        verifyNoInteractions(goalInsightClient, geminiService);
    }

    @Test
    void generateInsightsShouldRejectBlankUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> goalInsightService.generateInsights(" "));

        verifyNoInteractions(goalInsightClient, geminiService);
    }

    @Test
    void generateInsightsShouldHandleNullGoalsAndActivities() {
        when(goalInsightClient.getGoals(USER_ID))
                .thenReturn(null);

        when(goalInsightClient.getActivities(USER_ID))
                .thenReturn(null);

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn("Create a fitness goal to get started.");

        GoalInsightResponse response = goalInsightService.generateInsights(USER_ID);

        assertNotNull(response);
        assertEquals(
                "Create a fitness goal to get started.",
                response.getInsights());

        verify(geminiService).getChatAnswer(anyList());
    }

    @Test
    void generateInsightsShouldIncludeRecordedGoalAndActivityContext() {
        GoalInsightItem goal = mock(GoalInsightItem.class);

        when(goal.getTitle()).thenReturn("Weight Loss");
        when(goal.getGoalType()).thenReturn("WEIGHT_LOSS");
        when(goal.getTargetValue()).thenReturn(10.0);
        when(goal.getCurrentValue()).thenReturn(4.0);
        when(goal.getProgressPercentage()).thenReturn(40.0);
        when(goal.getStatus()).thenReturn("IN_PROGRESS");
        when(goal.getStartDate()).thenReturn(LocalDate.now().minusDays(10));
        when(goal.getEndDate()).thenReturn(LocalDate.now().plusDays(20));

        ActivityInsightItem activity = mock(ActivityInsightItem.class);

        when(activity.getType()).thenReturn("RUNNING");
        when(activity.getDuration()).thenReturn(30);
        when(activity.getCaloriesBurned()).thenReturn(250);
        when(activity.getStartTime())
                .thenReturn(LocalDateTime.now().minusDays(1));

        when(goalInsightClient.getGoals(USER_ID))
                .thenReturn(List.of(goal));

        when(goalInsightClient.getActivities(USER_ID))
                .thenReturn(List.of(activity));

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn("Keep following your current routine.");

        GoalInsightResponse response = goalInsightService.generateInsights(USER_ID);

        assertNotNull(response);

        ArgumentCaptor<List<Map<String, String>>> captor = ArgumentCaptor.forClass(List.class);

        verify(geminiService).getChatAnswer(captor.capture());

        List<Map<String, String>> messages = captor.getValue();

        assertEquals(2, messages.size());
        assertEquals("system", messages.get(0).get("role"));
        assertEquals("user", messages.get(1).get("role"));

        String context = messages.get(1).get("content");

        assertTrue(context.contains("Weight Loss"));
        assertTrue(context.contains("RUNNING"));
        assertTrue(context.contains("30 minutes"));
        assertTrue(context.contains("250"));
        assertTrue(context.contains("Total recorded workouts: 1"));
    }

    @Test
    void generateInsightsShouldHandleEmptyGoalsAndActivities() {
        when(goalInsightClient.getGoals(USER_ID))
                .thenReturn(List.of());

        when(goalInsightClient.getActivities(USER_ID))
                .thenReturn(List.of());

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn("Please create your first fitness goal.");

        goalInsightService.generateInsights(USER_ID);

        ArgumentCaptor<List<Map<String, String>>> captor = ArgumentCaptor.forClass(List.class);

        verify(geminiService).getChatAnswer(captor.capture());

        String context = captor.getValue().get(1).get("content");

        assertTrue(context.contains("No goals have been created."));
        assertTrue(context.contains("No recorded activities found."));
    }

    @Test
    void generateInsightsShouldPropagateAiServiceFailure() {
        when(goalInsightClient.getGoals(USER_ID))
                .thenReturn(List.of());

        when(goalInsightClient.getActivities(USER_ID))
                .thenReturn(List.of());

        when(geminiService.getChatAnswer(anyList()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> goalInsightService.generateInsights(USER_ID));

        assertEquals("AI service unavailable", exception.getMessage());
    }
}