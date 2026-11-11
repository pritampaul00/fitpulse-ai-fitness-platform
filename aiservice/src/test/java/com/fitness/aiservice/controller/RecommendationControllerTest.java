package com.fitness.aiservice.controller;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    private Recommendation createRecommendation() {
        return Recommendation.builder()
                .id("recommendation-123")
                .activityId("activity-123")
                .userId("user-123")
                .activityType("RUNNING")
                .recommendation("Good running performance")
                .improvements(List.of("Improve your pace"))
                .suggestions(List.of("Try interval training"))
                .safety(List.of("Stay hydrated"))
                .build();
    }

    @Test
    void getUserRecommendationsShouldReturnOwnRecommendations()
            throws Exception {

        when(recommendationService.getUserRecommendation("user-123"))
                .thenReturn(List.of(createRecommendation()));

        mockMvc.perform(get("/api/recommendations/user/user-123")
                        .header("X-User-ID", "user-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-123"))
                .andExpect(jsonPath("$[0].activityId").value("activity-123"));

        verify(recommendationService).getUserRecommendation("user-123");
    }

    @Test
    void getUserRecommendationsShouldRejectAnotherUser()
            throws Exception {

        mockMvc.perform(get("/api/recommendations/user/user-123")
                        .header("X-User-ID", "another-user"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recommendationService);
    }

    @Test
    void getUserRecommendationsShouldRequireUserHeader()
            throws Exception {

        mockMvc.perform(get("/api/recommendations/user/user-123"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recommendationService);
    }

    @Test
    void getActivityRecommendationShouldReturnOwnRecommendation()
            throws Exception {

        when(recommendationService.getActivityRecommendation(
                "activity-123", "user-123"))
                .thenReturn(createRecommendation());

        mockMvc.perform(get("/api/recommendations/activity/activity-123")
                        .header("X-User-ID", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.activityId").value("activity-123"));

        verify(recommendationService).getActivityRecommendation(
                "activity-123", "user-123");
    }

    @Test
    void getActivityRecommendationShouldRejectAnotherUsersActivity()
            throws Exception {

        when(recommendationService.getActivityRecommendation(
                "activity-123", "another-user"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "AI recommendation not found"
                ));

        mockMvc.perform(get("/api/recommendations/activity/activity-123")
                        .header("X-User-ID", "another-user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActivityRecommendationShouldRequireUserHeader()
            throws Exception {

        mockMvc.perform(get("/api/recommendations/activity/activity-123"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recommendationService);
    }
}
