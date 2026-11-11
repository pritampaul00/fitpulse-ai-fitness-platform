package com.fitness.aiservice.dto;

import lombok.Data;

@Data
public class ChatRequest {

    private String conversationId;

    private String message;
}
