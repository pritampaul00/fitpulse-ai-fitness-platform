package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.ActivityHistoryItem;

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
public class ActivityHistoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${activity.service.url}")
    private String activityServiceUrl;

    public List<ActivityHistoryItem> getUserActivities(String userId) {

        return webClientBuilder.build()
                .get()
                .uri(activityServiceUrl + "/api/activities")
                .header("X-User-ID", userId)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<List<ActivityHistoryItem>>() {}
                )
                .block(Duration.ofSeconds(8));
    }
}
