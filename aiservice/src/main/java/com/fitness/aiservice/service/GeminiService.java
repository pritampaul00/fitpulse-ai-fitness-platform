// package com.fitness.aiservice.service;

// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;
// import org.springframework.web.reactive.function.client.WebClientResponseException;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import reactor.util.retry.Retry;

// import java.time.Duration;
// import java.util.ArrayList;
// //import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
// import java.util.Map;

// @Service
// @Slf4j
// public class GeminiService {

//         private final WebClient webClient;

//         @Value("${groq.api.url}")
//         private String groqApiUrl;

//         @Value("${groq.api.key}")
//         private String groqApiKey;

//         @Value("${groq.api.model}")
//         private String groqModel;

//         public GeminiService(WebClient.Builder webClientBuilder) {
//                 this.webClient = webClientBuilder.build();
//         }

//         /**
//          * Existing AI recommendation functionality.
//          */
//         public String getAnswer(String question) {

//                 Map<String, Object> requestBody = Map.of(
//                                 "model", groqModel,
//                                 "messages", new Object[] {
//                                                 Map.of(
//                                                                 "role", "user",
//                                                                 "content", question)
//                                 },
//                                 "temperature", 0.7,
//                                 "max_tokens", 2048);

//                 return executeGroqRequest(requestBody);
//         }

//         /**
//          * Existing AI Coach functionality.
//          */
//         public String getChatAnswer(
//                         List<Map<String, String>> messages) {

//                 if (messages == null || messages.isEmpty()) {
//                         throw new IllegalArgumentException(
//                                         "Chat messages cannot be empty");
//                 }

//                 Map<String, Object> requestBody = Map.of(
//                                 "model", groqModel,
//                                 "messages", messages,
//                                 "temperature", 0.7,
//                                 "max_tokens", 1500);

//                 return executeGroqRequest(requestBody);
//         }

//         public List<String> getChatSuggestions(
//                         List<Map<String, String>> conversationMessages,
//                         String latestReply) {
//                 try {
//                         List<Map<String, String>> messages = new ArrayList<>(conversationMessages);

//                         messages.add(Map.of(
//                                         "role", "assistant",
//                                         "content", latestReply));

//                         messages.add(Map.of(
//                                         "role", "user",
//                                         "content", """
//                                                         Generate exactly 3 follow-up questions for this
//                                                         fitness coaching conversation.

//                                                         Rules:
//                                                         1. Base questions on the conversation and fitness context.
//                                                         2. Make each question specific and useful.
//                                                         3. Avoid repeating questions already asked.
//                                                         4. Keep each question under 100 characters.
//                                                         5. Return only a valid JSON array of 3 strings.
//                                                         6. Do not include markdown or explanations.

//                                                         Example:
//                                                         ["How should I improve my next workout?",
//                                                          "What recovery routine should I follow?",
//                                                          "How can I track my progress?"]
//                                                         """));

//                         Map<String, Object> requestBody = Map.of(
//                                         "model", groqModel,
//                                         "messages", messages,
//                                         "temperature", 0.5,
//                                         "max_tokens", 250);

//                         String response = executeGroqRequest(requestBody);

//                         if (response == null || response.isBlank()) {
//                                 return Collections.emptyList();
//                         }

//                         String cleaned = response
//                                         .replaceAll("(?s)^```(?:json)?\\s*", "")
//                                         .replaceAll("\\s*```$", "")
//                                         .trim();

//                         int start = cleaned.indexOf('[');
//                         int end = cleaned.lastIndexOf(']');

//                         if (start < 0 || end <= start) {
//                                 return Collections.emptyList();
//                         }

//                         String json = cleaned.substring(start, end + 1);

//                         List<String> suggestions = new ObjectMapper().readValue(
//                                         json,
//                                         new TypeReference<List<String>>() {
//                                         });

//                         return suggestions.stream()
//                                         .filter(item -> item != null && !item.isBlank())
//                                         .map(String::trim)
//                                         .limit(3)
//                                         .toList();

//                 } catch (Exception e) {
//                         log.warn(
//                                         "Could not generate AI Coach suggestions: {}",
//                                         e.getMessage());

//                         return Collections.emptyList();
//                 }
//         }

//         /**
//          * Dedicated workout generation method.
//          * Uses a larger output-token limit because workout plans
//          * contain multiple sessions and exercises.
//          */
//         public String getWorkoutPlanAnswer(
//                         List<Map<String, String>> messages) {

//                 if (messages == null || messages.isEmpty()) {
//                         throw new IllegalArgumentException(
//                                         "Workout generation messages cannot be empty");
//                 }

//                 Map<String, Object> requestBody = Map.of(
//                                 "model", groqModel,
//                                 "messages", messages,
//                                 "temperature", 0.2,
//                                 "max_tokens", 4096);

//                 return executeGroqRequest(requestBody);
//         }

//         // public List<String> getChatSuggestions(
//         // List<Map<String, String>> conversationMessages,
//         // String latestReply) {
//         // try {
//         // List<Map<String, String>> messages = new ArrayList<>(conversationMessages);

//         // messages.add(Map.of(
//         // "role", "assistant",
//         // "content", latestReply));

//         // messages.add(Map.of(
//         // "role", "user",
//         // "content", """
//         // Based on the conversation and the user's fitness context,
//         // generate exactly 3 useful follow-up questions.

//         // Requirements:
//         // - Make each question specific to this conversation.
//         // - Prefer practical questions the user may naturally ask next.
//         // - Avoid repeating questions already asked.
//         // - Keep each question under 100 characters.
//         // - Return only a valid JSON array of 3 strings.
//         // - Do not include markdown or explanations.

//         // Example:
//         // ["How should I adjust my next workout?",
//         // "What recovery routine should I follow?",
//         // "How can I track my progress?"]
//         // """));

//         // Map<String, Object> requestBody = Map.of(
//         // "model", groqModel,
//         // "messages", messages,
//         // "temperature", 0.5,
//         // "max_tokens", 250);

//         // String response = executeGroqRequest(requestBody);

//         // if (response == null || response.isBlank()) {
//         // return Collections.emptyList();
//         // }

//         // String cleaned = response
//         // .replaceAll("(?s)^```(?:json)?\\s*", "")
//         // .replaceAll("\\s*```$", "")
//         // .trim();

//         // int start = cleaned.indexOf('[');
//         // int end = cleaned.lastIndexOf(']');

//         // if (start < 0 || end <= start) {
//         // return Collections.emptyList();
//         // }

//         // String json = cleaned.substring(start, end + 1);

//         // return new ObjectMapper()
//         // .readValue(
//         // json,
//         // new TypeReference<List<String>>() {
//         // })
//         // .stream()
//         // .filter(item -> item != null && !item.isBlank())
//         // .map(String::trim)
//         // .limit(3)
//         // .toList();

//         // } catch (Exception e) {
//         // log.warn("Could not generate AI Coach follow-up suggestions: {}",
//         // e.getMessage());

//         // return Collections.emptyList();
//         // }
//         // }

//         /**
//          * Shared Groq API request implementation.
//          */
//         private String executeGroqRequest(
//                         Map<String, Object> requestBody) {

//                 return webClient.post()
//                                 .uri(groqApiUrl)
//                                 .header(
//                                                 "Authorization",
//                                                 "Bearer " + groqApiKey)
//                                 .header(
//                                                 "Content-Type",
//                                                 "application/json")
//                                 .bodyValue(requestBody)
//                                 .retrieve()
//                                 .bodyToMono(Map.class)
//                                 .map(response -> {

//                                         List<Map<String, Object>> choices = (List<Map<String, Object>>) response
//                                                         .get("choices");

//                                         if (choices == null || choices.isEmpty()) {
//                                                 throw new IllegalStateException(
//                                                                 "Groq returned no choices");
//                                         }

//                                         Map<String, Object> choice = choices.get(0);

//                                         String finishReason = String.valueOf(choice.get("finish_reason"));

//                                         if ("length".equalsIgnoreCase(finishReason)) {
//                                                 throw new IllegalStateException(
//                                                                 "Groq reached its output-token limit. " +
//                                                                                 "The generated response was truncated.");
//                                         }

//                                         Map<String, Object> message = (Map<String, Object>) choice.get("message");

//                                         if (message == null ||
//                                                         message.get("content") == null) {

//                                                 throw new IllegalStateException(
//                                                                 "Groq returned empty content");
//                                         }

//                                         String content = message.get("content").toString();

//                                         if (content.isBlank()) {
//                                                 throw new IllegalStateException(
//                                                                 "Groq returned blank content");
//                                         }

//                                         log.info(
//                                                         "Groq response received. Model: {}, Finish reason: {}, Characters: {}",
//                                                         groqModel,
//                                                         finishReason,
//                                                         content.length());

//                                         return content;
//                                 })
//                                 .doOnError(error -> {

//                                         if (error instanceof WebClientResponseException ex) {

//                                                 log.error(
//                                                                 "Groq API error. Status: {}, Response: {}",
//                                                                 ex.getStatusCode(),
//                                                                 ex.getResponseBodyAsString());

//                                         } else {

//                                                 log.error(
//                                                                 "Groq request failed: {}",
//                                                                 error.getMessage());
//                                         }
//                                 })
//                                 .retryWhen(
//                                                 Retry.backoff(
//                                                                 2,
//                                                                 Duration.ofSeconds(2))
//                                                                 .maxBackoff(Duration.ofSeconds(5))
//                                                                 .filter(this::isRetryable)
//                                                                 .doBeforeRetry(signal -> log.warn(
//                                                                                 "Retrying Groq request. Attempt: {}",
//                                                                                 signal.totalRetries() + 1)))
//                                 .block();
//         }

//         /**
//          * Retry only transient server errors.
//          */
//         private boolean isRetryable(Throwable error) {

//                 if (error instanceof WebClientResponseException ex) {

//                         int status = ex.getStatusCode().value();

//                         return status == 500 ||
//                                         status == 502 ||
//                                         status == 503 ||
//                                         status == 504;
//                 }

//                 return false;
//         }
// }



package com.fitness.aiservice.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

        private final WebClient webClient;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Value("${groq.api.url}")
        private String groqApiUrl;

        @Value("${groq.api.key}")
        private String groqApiKey;

        @Value("${groq.api.model}")
        private String groqModel;

        public GeminiService(WebClient.Builder webClientBuilder) {
                this.webClient = webClientBuilder.build();
        }

        /**
         * Existing AI recommendation functionality.
         */
        public String getAnswer(String question) {

                Map<String, Object> requestBody = Map.of(
                                "model", groqModel,
                                "messages", new Object[] {
                                                Map.of(
                                                                "role", "user",
                                                                "content", question)
                                },
                                "temperature", 0.7,
                                "max_tokens", 2048);

                return executeGroqRequest(requestBody);
        }

        /**
         * Existing AI Coach functionality.
         */
        public String getChatAnswer(
                        List<Map<String, String>> messages) {

                if (messages == null || messages.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Chat messages cannot be empty");
                }

                Map<String, Object> requestBody = Map.of(
                                "model", groqModel,
                                "messages", messages,
                                "temperature", 0.7,
                                "max_tokens", 1500);

                return executeGroqRequest(requestBody);
        }

        /**
         * Streams AI Coach response tokens from Groq.
         */
        public Flux<String> streamChatAnswer(
                        List<Map<String, String>> messages) {

                if (messages == null || messages.isEmpty()) {
                        return Flux.error(new IllegalArgumentException(
                                        "Chat messages cannot be empty"));
                }

                Map<String, Object> requestBody = Map.of(
                                "model", groqModel,
                                "messages", messages,
                                "temperature", 0.7,
                                "max_tokens", 1500,
                                "stream", true);

                return webClient.post()
                                .uri(groqApiUrl)
                                .header("Authorization", "Bearer " + groqApiKey)
                                .header("Content-Type", "application/json")
                                .header("Accept", "text/event-stream")
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                                .takeUntil(event -> "[DONE]".equals(event.data()))
                                .map(ServerSentEvent::data)
                                .filter(data -> data != null && !data.isBlank() && !"[DONE]".equals(data))
                                .map(this::extractStreamedContent)
                                .filter(content -> !content.isEmpty())
                                .doOnError(error -> log.error(
                                                "Groq streaming request failed: {}",
                                                error.getMessage()));
        }

        private String extractStreamedContent(String eventData) {
                try {
                        JsonNode root = objectMapper.readTree(eventData);
                        JsonNode content = root.path("choices")
                                        .path(0)
                                        .path("delta")
                                        .path("content");

                        return content.isMissingNode() || content.isNull()
                                        ? ""
                                        : content.asText();
                } catch (Exception exception) {
                        log.debug("Skipping unrecognized Groq stream event: {}", eventData);
                        return "";
                }
        }

        public List<String> getChatSuggestions(
                        List<Map<String, String>> conversationMessages,
                        String latestReply) {
                try {
                        List<Map<String, String>> messages = new ArrayList<>(conversationMessages);

                        messages.add(Map.of(
                                        "role", "assistant",
                                        "content", latestReply));

                        messages.add(Map.of(
                                        "role", "user",
                                        "content", """
                                                        Generate exactly 3 follow-up questions for this
                                                        fitness coaching conversation.

                                                        Rules:
                                                        1. Base questions on the conversation and fitness context.
                                                        2. Make each question specific and useful.
                                                        3. Avoid repeating questions already asked.
                                                        4. Keep each question under 100 characters.
                                                        5. Return only a valid JSON array of 3 strings.
                                                        6. Do not include markdown or explanations.

                                                        Example:
                                                        ["How should I improve my next workout?",
                                                         "What recovery routine should I follow?",
                                                         "How can I track my progress?"]
                                                        """));

                        Map<String, Object> requestBody = Map.of(
                                        "model", groqModel,
                                        "messages", messages,
                                        "temperature", 0.5,
                                        "max_tokens", 250);

                        String response = executeGroqRequest(requestBody);

                        if (response == null || response.isBlank()) {
                                return Collections.emptyList();
                        }

                        String cleaned = response
                                        .replaceAll("(?s)^```(?:json)?\\s*", "")
                                        .replaceAll("\\s*```$", "")
                                        .trim();

                        int start = cleaned.indexOf('[');
                        int end = cleaned.lastIndexOf(']');

                        if (start < 0 || end <= start) {
                                return Collections.emptyList();
                        }

                        String json = cleaned.substring(start, end + 1);

                        List<String> suggestions = new ObjectMapper().readValue(
                                        json,
                                        new TypeReference<List<String>>() {
                                        });

                        return suggestions.stream()
                                        .filter(item -> item != null && !item.isBlank())
                                        .map(String::trim)
                                        .limit(3)
                                        .toList();

                } catch (Exception e) {
                        log.warn(
                                        "Could not generate AI Coach suggestions: {}",
                                        e.getMessage());

                        return Collections.emptyList();
                }
        }

        /**
         * Dedicated workout generation method.
         * Uses a larger output-token limit because workout plans
         * contain multiple sessions and exercises.
         */
        public String getWorkoutPlanAnswer(
                        List<Map<String, String>> messages) {

                if (messages == null || messages.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Workout generation messages cannot be empty");
                }

                Map<String, Object> requestBody = Map.of(
                                "model", groqModel,
                                "messages", messages,
                                "temperature", 0.2,
                                "max_tokens", 4096);

                return executeGroqRequest(requestBody);
        }

        // public List<String> getChatSuggestions(
        // List<Map<String, String>> conversationMessages,
        // String latestReply) {
        // try {
        // List<Map<String, String>> messages = new ArrayList<>(conversationMessages);

        // messages.add(Map.of(
        // "role", "assistant",
        // "content", latestReply));

        // messages.add(Map.of(
        // "role", "user",
        // "content", """
        // Based on the conversation and the user's fitness context,
        // generate exactly 3 useful follow-up questions.

        // Requirements:
        // - Make each question specific to this conversation.
        // - Prefer practical questions the user may naturally ask next.
        // - Avoid repeating questions already asked.
        // - Keep each question under 100 characters.
        // - Return only a valid JSON array of 3 strings.
        // - Do not include markdown or explanations.

        // Example:
        // ["How should I adjust my next workout?",
        // "What recovery routine should I follow?",
        // "How can I track my progress?"]
        // """));

        // Map<String, Object> requestBody = Map.of(
        // "model", groqModel,
        // "messages", messages,
        // "temperature", 0.5,
        // "max_tokens", 250);

        // String response = executeGroqRequest(requestBody);

        // if (response == null || response.isBlank()) {
        // return Collections.emptyList();
        // }

        // String cleaned = response
        // .replaceAll("(?s)^```(?:json)?\\s*", "")
        // .replaceAll("\\s*```$", "")
        // .trim();

        // int start = cleaned.indexOf('[');
        // int end = cleaned.lastIndexOf(']');

        // if (start < 0 || end <= start) {
        // return Collections.emptyList();
        // }

        // String json = cleaned.substring(start, end + 1);

        // return new ObjectMapper()
        // .readValue(
        // json,
        // new TypeReference<List<String>>() {
        // })
        // .stream()
        // .filter(item -> item != null && !item.isBlank())
        // .map(String::trim)
        // .limit(3)
        // .toList();

        // } catch (Exception e) {
        // log.warn("Could not generate AI Coach follow-up suggestions: {}",
        // e.getMessage());

        // return Collections.emptyList();
        // }
        // }

        /**
         * Shared Groq API request implementation.
         */
        private String executeGroqRequest(
                        Map<String, Object> requestBody) {

                return webClient.post()
                                .uri(groqApiUrl)
                                .header(
                                                "Authorization",
                                                "Bearer " + groqApiKey)
                                .header(
                                                "Content-Type",
                                                "application/json")
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> {

                                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response
                                                        .get("choices");

                                        if (choices == null || choices.isEmpty()) {
                                                throw new IllegalStateException(
                                                                "Groq returned no choices");
                                        }

                                        Map<String, Object> choice = choices.get(0);

                                        String finishReason = String.valueOf(choice.get("finish_reason"));

                                        if ("length".equalsIgnoreCase(finishReason)) {
                                                throw new IllegalStateException(
                                                                "Groq reached its output-token limit. " +
                                                                                "The generated response was truncated.");
                                        }

                                        Map<String, Object> message = (Map<String, Object>) choice.get("message");

                                        if (message == null ||
                                                        message.get("content") == null) {

                                                throw new IllegalStateException(
                                                                "Groq returned empty content");
                                        }

                                        String content = message.get("content").toString();

                                        if (content.isBlank()) {
                                                throw new IllegalStateException(
                                                                "Groq returned blank content");
                                        }

                                        log.info(
                                                        "Groq response received. Model: {}, Finish reason: {}, Characters: {}",
                                                        groqModel,
                                                        finishReason,
                                                        content.length());

                                        return content;
                                })
                                .doOnError(error -> {

                                        if (error instanceof WebClientResponseException ex) {

                                                log.error(
                                                                "Groq API error. Status: {}, Response: {}",
                                                                ex.getStatusCode(),
                                                                ex.getResponseBodyAsString());

                                        } else {

                                                log.error(
                                                                "Groq request failed: {}",
                                                                error.getMessage());
                                        }
                                })
                                .retryWhen(
                                                Retry.backoff(
                                                                2,
                                                                Duration.ofSeconds(2))
                                                                .maxBackoff(Duration.ofSeconds(5))
                                                                .filter(this::isRetryable)
                                                                .doBeforeRetry(signal -> log.warn(
                                                                                "Retrying Groq request. Attempt: {}",
                                                                                signal.totalRetries() + 1)))
                                .block();
        }

        /**
         * Retry only transient server errors.
         */
        private boolean isRetryable(Throwable error) {

                if (error instanceof WebClientResponseException ex) {

                        int status = ex.getStatusCode().value();

                        return status == 500 ||
                                        status == 502 ||
                                        status == 503 ||
                                        status == 504;
                }

                return false;
        }
}