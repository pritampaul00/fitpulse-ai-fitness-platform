package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityMessageListenerTest {

    @Mock
    private ActivityAIService aiService;

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private ActivityMessageListener listener;

    private Activity activity;
    private Recommendation recommendation;

    @BeforeEach
    void setUp() {
        activity = new Activity();
        activity.setId("activity-123");
        activity.setUserId("user-123");
        activity.setType("RUNNING");

        recommendation = new Recommendation();
        recommendation.setId("recommendation-123");
        recommendation.setActivityId("activity-123");
        recommendation.setUserId("user-123");
    }

    @Test
    void processActivityShouldGenerateAndSaveRecommendation() {
        when(aiService.generateRecommendation(activity))
                .thenReturn(recommendation);

        when(recommendationRepository.findByActivityId("activity-123"))
                .thenReturn(Optional.empty());

        when(recommendationRepository.save(recommendation))
                .thenReturn(recommendation);

        assertDoesNotThrow(() -> listener.processActivity(activity));

        verify(aiService).generateRecommendation(activity);
        verify(recommendationRepository).findByActivityId("activity-123");
        verify(recommendationRepository).save(recommendation);
    }

    @Test
    void processActivityShouldUpdateExistingRecommendation() {
        Recommendation existing = new Recommendation();
        existing.setId("existing-recommendation-456");

        when(aiService.generateRecommendation(activity))
                .thenReturn(recommendation);

        when(recommendationRepository.findByActivityId("activity-123"))
                .thenReturn(Optional.of(existing));

        when(recommendationRepository.save(recommendation))
                .thenReturn(recommendation);

        listener.processActivity(activity);

        assertEquals(
                "existing-recommendation-456",
                recommendation.getId()
        );

        verify(recommendationRepository).save(recommendation);
    }

    @Test
    void processActivityShouldRejectNullRecommendation() {
        when(aiService.generateRecommendation(activity))
                .thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> listener.processActivity(activity)
        );

        verify(recommendationRepository, never())
                .save(any(Recommendation.class));
    }

    @Test
    void processActivityShouldPropagateAiServiceFailure() {
        RuntimeException exception =
                new RuntimeException("AI service unavailable");

        when(aiService.generateRecommendation(activity))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> listener.processActivity(activity)
        );

        assertEquals("AI service unavailable", thrown.getMessage());

        verify(recommendationRepository, never())
                .save(any(Recommendation.class));
    }

    @Test
    void processActivityShouldPropagateRepositorySaveFailure() {
        when(aiService.generateRecommendation(activity))
                .thenReturn(recommendation);

        when(recommendationRepository.findByActivityId("activity-123"))
                .thenReturn(Optional.empty());

        when(recommendationRepository.save(recommendation))
                .thenThrow(new RuntimeException("MongoDB unavailable"));

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> listener.processActivity(activity)
        );

        assertEquals("MongoDB unavailable", thrown.getMessage());
    }

    @Test
    void processActivityShouldNotOverwriteExistingIdWhenNoExistingRecord() {
        recommendation.setId("new-recommendation-123");

        when(aiService.generateRecommendation(activity))
                .thenReturn(recommendation);

        when(recommendationRepository.findByActivityId("activity-123"))
                .thenReturn(Optional.empty());

        when(recommendationRepository.save(recommendation))
                .thenReturn(recommendation);

        listener.processActivity(activity);

        assertEquals("new-recommendation-123", recommendation.getId());
    }
}