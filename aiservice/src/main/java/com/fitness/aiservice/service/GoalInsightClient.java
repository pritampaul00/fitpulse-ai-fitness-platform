// package com.fitness.aiservice.service;

// import com.fitness.aiservice.dto.ActivityInsightItem;
// import com.fitness.aiservice.dto.GoalInsightItem;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.ParameterizedTypeReference;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Component;
// import org.springframework.web.reactive.function.client.WebClient;

// import java.time.Duration;
// import java.util.List;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class GoalInsightClient {

//     private final WebClient.Builder webClientBuilder;

//     @Value("${activity.service.url}")
//     private String activityServiceUrl;

//     public List<GoalInsightItem> getGoals(String userId) {
//         return webClientBuilder.build()
//                 .get()
//                 .uri(activityServiceUrl + "/api/goals")
//                 .header("X-User-ID", userId)
//                 .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//                 .retrieve()
//                 .bodyToMono(
//                         new ParameterizedTypeReference<List<GoalInsightItem>>() {}
//                 )
//                 .block(Duration.ofSeconds(8));
//     }

//     public List<ActivityInsightItem> getActivities(String userId) {
//         return webClientBuilder.build()
//                 .get()
//                 .uri(activityServiceUrl + "/api/activities")
//                 .header("X-User-ID", userId)
//                 .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//                 .retrieve()
//                 .bodyToMono(
//                         new ParameterizedTypeReference<List<ActivityInsightItem>>() {}
//                 )
//                 .block(Duration.ofSeconds(8));
//     }
// }

package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.ActivityInsightItem;
import com.fitness.aiservice.dto.GoalInsightItem;
import com.fitness.aiservice.dto.WorkoutPlanContextItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoalInsightClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${activity.service.url}")
    private String activityServiceUrl;

    public List<GoalInsightItem> getGoals(String userId) {

        return webClientBuilder.build()
                .get()
                .uri(activityServiceUrl + "/api/goals")
                .header("X-User-ID", userId)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<List<GoalInsightItem>>() {}
                )
                .block(Duration.ofSeconds(8));
    }

    public List<ActivityInsightItem> getActivities(String userId) {

        return webClientBuilder.build()
                .get()
                .uri(activityServiceUrl + "/api/activities")
                .header("X-User-ID", userId)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<List<ActivityInsightItem>>() {}
                )
                .block(Duration.ofSeconds(8));
    }

    public List<WorkoutPlanContextItem> getWorkoutPlans(String userId) {

        log.info("Fetching existing workout plans for user: {}", userId);

        return webClientBuilder.build()
                .get()
                .uri(activityServiceUrl + "/api/workout-plans")
                .header("X-User-ID", userId)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<List<WorkoutPlanContextItem>>() {}
                )
                .block(Duration.ofSeconds(8));
    }
}