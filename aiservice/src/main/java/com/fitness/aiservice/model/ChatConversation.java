package com.fitness.aiservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "coach_conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    @Id
    private String id;

    private String userId;

    private String title;

    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {

        private String role;

        private String content;

        private LocalDateTime timestamp;
    }
}
