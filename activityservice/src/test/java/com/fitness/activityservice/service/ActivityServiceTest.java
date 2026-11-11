
package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

        @Mock
        private ActivityRepository activityRepository;

        @Mock
        private UserValidationService userValidationService;

        @Mock
        private RabbitTemplate rabbitTemplate;

        @InjectMocks
        private ActivityService activityService;

        private Activity activity;
        private ActivityRequest request;

        private final String userId = "user-123";
        private final String activityId = "activity-123";

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(
                                activityService, "exchange", "fitness.exchange");

                ReflectionTestUtils.setField(
                                activityService, "routingKey", "activity.created");

                request = new ActivityRequest();
                request.setUserId(userId);
                request.setType(ActivityType.RUNNING);
                request.setDuration(30);
                request.setCaloriesBurned(250);
                request.setStartTime(LocalDateTime.now());

                activity = Activity.builder()
                                .id(activityId)
                                .userId(userId)
                                .type(ActivityType.RUNNING)
                                .duration(30)
                                .caloriesBurned(250)
                                .startTime(request.getStartTime())
                                .build();
        }

        @Test
        void trackActivityShouldSaveAndPublishActivity() {
                when(userValidationService.validateUser(userId))
                                .thenReturn(true);

                when(activityRepository.save(any(Activity.class)))
                                .thenReturn(activity);

                ActivityResponse response = activityService.trackActivity(request);

                assertNotNull(response);
                assertEquals(activityId, response.getId());
                assertEquals(userId, response.getUserId());
                assertEquals(ActivityType.RUNNING, response.getType());
                assertEquals(30, response.getDuration());
                assertEquals(250, response.getCaloriesBurned());

                verify(activityRepository).save(any(Activity.class));

                verify(rabbitTemplate).convertAndSend(
                                eq("fitness.exchange"),
                                eq("activity.created"),
                                any(Object.class));
        }

        @Test
        void trackActivityShouldRejectInvalidUser() {
                when(userValidationService.validateUser(userId))
                                .thenReturn(false);

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> activityService.trackActivity(request));

                assertTrue(exception.getMessage().contains("Invalid User"));

                verify(activityRepository, never()).save(any(Activity.class));

                verify(rabbitTemplate, never()).convertAndSend(
                                anyString(),
                                anyString(),
                                any(Object.class));
        }

        @Test
        void trackActivityShouldStillReturnResponseWhenRabbitMqFails() {
                when(userValidationService.validateUser(userId))
                                .thenReturn(true);

                when(activityRepository.save(any(Activity.class)))
                                .thenReturn(activity);

                doThrow(new RuntimeException("RabbitMQ unavailable"))
                                .when(rabbitTemplate)
                                .convertAndSend(
                                                anyString(),
                                                anyString(),
                                                any(Object.class));

                ActivityResponse response = activityService.trackActivity(request);

                assertNotNull(response);
                assertEquals(activityId, response.getId());

                verify(activityRepository).save(any(Activity.class));
        }

        @Test
        void getUserActivitiesShouldReturnUserActivities() {
                when(activityRepository.findByUserId(userId))
                                .thenReturn(List.of(activity));

                List<ActivityResponse> responses = activityService.getUserActivities(userId);

                assertEquals(1, responses.size());
                assertEquals(activityId, responses.get(0).getId());
                assertEquals(userId, responses.get(0).getUserId());

                verify(activityRepository).findByUserId(userId);
        }

        @Test
        void getUserActivitiesShouldReturnEmptyListWhenNoActivitiesExist() {
                when(activityRepository.findByUserId(userId))
                                .thenReturn(List.of());

                List<ActivityResponse> responses = activityService.getUserActivities(userId);

                assertNotNull(responses);
                assertTrue(responses.isEmpty());
        }

        @Test
        void getActivityByIdShouldReturnActivityForOwner() {
                when(activityRepository.findByIdAndUserId(activityId, userId))
                                .thenReturn(Optional.of(activity));

                ActivityResponse response = activityService.getActivityById(activityId, userId);

                assertNotNull(response);
                assertEquals(activityId, response.getId());
                assertEquals(userId, response.getUserId());
                assertEquals(ActivityType.RUNNING, response.getType());

                verify(activityRepository).findByIdAndUserId(activityId, userId);
        }

        @Test
        void getActivityByIdShouldRejectAnotherUsersActivity() {
                String anotherUserId = "another-user";

                when(activityRepository.findByIdAndUserId(
                                activityId, anotherUserId))
                                .thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> activityService.getActivityById(
                                                activityId, anotherUserId));

                assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

                verify(activityRepository).findByIdAndUserId(
                                activityId, anotherUserId);
        }

        @Test
        void regenerateRecommendationShouldPublishForActivityOwner() {
                when(activityRepository.findByIdAndUserId(activityId, userId))
                                .thenReturn(Optional.of(activity));

                activityService.regenerateRecommendation(activityId, userId);

                verify(rabbitTemplate).convertAndSend(
                                eq("fitness.exchange"),
                                eq("activity.created"),
                                any(Object.class));
        }

        @Test
        void regenerateRecommendationShouldRejectUnauthorizedUser() {
                when(activityRepository.findByIdAndUserId(activityId, userId))
                                .thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> activityService.regenerateRecommendation(
                                                activityId, userId));

                assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

                verify(rabbitTemplate, never()).convertAndSend(
                                anyString(),
                                anyString(),
                                any(Object.class));
        }

        @Test
        void regenerateRecommendationShouldReturnServiceUnavailableWhenRabbitMqFails() {
                when(activityRepository.findByIdAndUserId(activityId, userId))
                                .thenReturn(Optional.of(activity));

                doThrow(new RuntimeException("RabbitMQ unavailable"))
                                .when(rabbitTemplate)
                                .convertAndSend(
                                                anyString(),
                                                anyString(),
                                                any(Object.class));

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> activityService.regenerateRecommendation(
                                                activityId, userId));

                assertEquals(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                exception.getStatusCode());
        }
}
