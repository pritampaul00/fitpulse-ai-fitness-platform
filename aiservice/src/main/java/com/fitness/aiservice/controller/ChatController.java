// package com.fitness.aiservice.controller;

// import com.fitness.aiservice.dto.ChatRequest;
// import com.fitness.aiservice.dto.ChatResponse;
// import com.fitness.aiservice.model.ChatConversation;
// import com.fitness.aiservice.service.ChatService;

// import lombok.RequiredArgsConstructor;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// import java.util.List;

// @RestController
// @RequestMapping("/api/coach")
// @RequiredArgsConstructor
// public class ChatController {

//     private final ChatService chatService;

//     @PostMapping("/chat")
//     public ResponseEntity<ChatResponse> sendMessage(
//             @RequestHeader("X-User-ID") String userId,
//             @RequestBody ChatRequest request) {

//         return ResponseEntity.ok(
//                 chatService.sendMessage(userId, request)
//         );
//     }

//     @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//     public SseEmitter streamMessage(
//             @RequestHeader("X-User-ID") String userId,
//             @RequestBody ChatRequest request) {

//         return chatService.streamMessage(userId, request);
//     }

//     @GetMapping("/conversations")
//     public ResponseEntity<List<ChatConversation>> getConversations(
//             @RequestHeader("X-User-ID") String userId) {

//         return ResponseEntity.ok(
//                 chatService.getConversations(userId)
//         );
//     }

//     @GetMapping("/conversations/{conversationId}")
//     public ResponseEntity<ChatConversation> getConversation(
//             @RequestHeader("X-User-ID") String userId,
//             @PathVariable String conversationId) {

//         return ResponseEntity.ok(
//                 chatService.getConversation(userId, conversationId)
//         );
//     }

//     @DeleteMapping("/conversations/{conversationId}")
//     public ResponseEntity<Void> deleteConversation(
//             @RequestHeader("X-User-ID") String userId,
//             @PathVariable String conversationId) {

//         chatService.deleteConversation(userId, conversationId);

//         return ResponseEntity.noContent().build();
//     }
// }


package com.fitness.aiservice.controller;

import com.fitness.aiservice.dto.ChatRequest;
import com.fitness.aiservice.dto.ChatResponse;
import com.fitness.aiservice.model.ChatConversation;
import com.fitness.aiservice.service.ChatService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody ChatRequest request) {

        return ResponseEntity.ok(
                chatService.sendMessage(userId, request)
        );
    }

    @PostMapping(
            value = "/chat/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamMessage(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody ChatRequest request) {

        return chatService.streamMessage(userId, request);
    }

    @PutMapping(
            value = "/conversations/{conversationId}/messages/{messageIndex}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter editMessage(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String conversationId,
            @PathVariable int messageIndex,
            @RequestBody ChatRequest request) {

        return chatService.editMessageAndStream(
                userId,
                conversationId,
                messageIndex,
                request
        );
    }

    @PostMapping(
            value = "/conversations/{conversationId}/messages/{messageIndex}/regenerate",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter regenerateResponse(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String conversationId,
            @PathVariable int messageIndex) {

        return chatService.regenerateResponseAndStream(
                userId,
                conversationId,
                messageIndex
        );
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ChatConversation>> getConversations(
            @RequestHeader("X-User-ID") String userId) {

        return ResponseEntity.ok(
                chatService.getConversations(userId)
        );
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatConversation> getConversation(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String conversationId) {

        return ResponseEntity.ok(
                chatService.getConversation(userId, conversationId)
        );
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String conversationId) {

        chatService.deleteConversation(userId, conversationId);

        return ResponseEntity.noContent().build();
    }
}
