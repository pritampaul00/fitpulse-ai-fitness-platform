package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity) {

        log.info("Received activity for processing: {}", activity.getId());

        try {
            // Generate AI recommendation using Groq
            Recommendation recommendation = aiService.generateRecommendation(activity);

            if (recommendation == null) {
                throw new IllegalStateException(
                        "AI service returned a null recommendation");
            }

            // Save recommendation in MongoDB
            recommendationRepository.findByActivityId(activity.getId())
                    .ifPresent(existing -> recommendation.setId(existing.getId()));
            Recommendation savedRecommendation = recommendationRepository.save(recommendation);

            // Confirm successful persistence
            log.info(
                    "AI recommendation saved successfully. Activity ID: {}, Recommendation ID: {}",
                    savedRecommendation.getActivityId(),
                    savedRecommendation.getId());

        } catch (Exception e) {

            log.error(
                    "Failed to process activity ID: {}. Error: {}",
                    activity.getId(),
                    e.getMessage(),
                    e);

            // Propagate the exception so RabbitMQ can handle the failed message
            throw e;
        }
    }
}
