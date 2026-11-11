
package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityAIServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private final String userId = "user-123";
    private final String activityId = "activity-123";

    @Test
    void getUserRecommendationShouldReturnUserRecommendations() {
        Recommendation recommendation = Recommendation.builder()
                .id("recommendation-123")
                .activityId(activityId)
                .userId(userId)
                .activityType("RUNNING")
                .recommendation("Good running performance")
                .improvements(List.of("Improve your pace"))
                .suggestions(List.of("Try interval training"))
                .safety(List.of("Stay hydrated"))
                .build();

        when(recommendationRepository.findByUserId(userId))
                .thenReturn(List.of(recommendation));

        List<Recommendation> result =
                recommendationService.getUserRecommendation(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(activityId, result.get(0).getActivityId());

        verify(recommendationRepository).findByUserId(userId);
    }

    @Test
    void getUserRecommendationShouldReturnEmptyListWhenNoRecommendationsExist() {
        when(recommendationRepository.findByUserId(userId))
                .thenReturn(List.of());

        List<Recommendation> result =
                recommendationService.getUserRecommendation(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(recommendationRepository).findByUserId(userId);
    }

    @Test
    void getActivityRecommendationShouldReturnRecommendationForOwner() {
        Recommendation recommendation = Recommendation.builder()
                .id("recommendation-123")
                .activityId(activityId)
                .userId(userId)
                .activityType("RUNNING")
                .recommendation("Good running performance")
                .improvements(List.of("Improve your pace"))
                .suggestions(List.of("Try interval training"))
                .safety(List.of("Stay hydrated"))
                .build();

        when(recommendationRepository.findByActivityIdAndUserId(activityId, userId))
                .thenReturn(Optional.of(recommendation));

        Recommendation result =
                recommendationService.getActivityRecommendation(activityId, userId);

        assertNotNull(result);
        assertEquals(activityId, result.getActivityId());
        assertEquals(userId, result.getUserId());

        verify(recommendationRepository)
                .findByActivityIdAndUserId(activityId, userId);
    }

    @Test
    void getActivityRecommendationShouldThrowNotFoundWhenMissingOrNotOwned() {
        when(recommendationRepository.findByActivityIdAndUserId(activityId, userId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recommendationService.getActivityRecommendation(activityId, userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(recommendationRepository)
                .findByActivityIdAndUserId(activityId, userId);
    }
}
