package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.ChatRequest;
import com.fitness.aiservice.dto.ChatResponse;
import com.fitness.aiservice.model.ChatConversation;
import com.fitness.aiservice.repository.ChatConversationRepository;
import com.fitness.aiservice.repository.RecommendationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private ChatConversationRepository conversationRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private ActivityHistoryClient activityHistoryClient;

    private ChatService chatService;

    private static final String USER_ID = "user-123";
    private static final String CONVERSATION_ID = "conversation-123";

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                geminiService,
                conversationRepository,
                recommendationRepository,
                activityHistoryClient
        );
    }

    private void mockContext() {
        when(activityHistoryClient.getUserActivities(USER_ID))
                .thenReturn(List.of());

        when(recommendationRepository
                .findTop5ByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of());
    }

    private ChatRequest createRequest(String message) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        return request;
    }

    private ChatConversation createConversation() {
        return ChatConversation.builder()
                .id(CONVERSATION_ID)
                .userId(USER_ID)
                .title("Existing conversation")
                .messages(new ArrayList<>())
                .build();
    }

    @Test
    void sendMessageShouldCreateConversationAndReturnAiResponse() {
        mockContext();

        ChatRequest request = createRequest("  Give me a workout plan  ");

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn("Start with a 20-minute walk.");

        when(geminiService.getChatSuggestions(anyList(), anyString()))
                .thenReturn(List.of(
                        "How often should I walk?",
                        "What should I do next?"
                ));

        when(conversationRepository.save(any(ChatConversation.class)))
                .thenAnswer(invocation -> {
                    ChatConversation conversation = invocation.getArgument(0);
                    conversation.setId(CONVERSATION_ID);
                    return conversation;
                });

        ChatResponse response = chatService.sendMessage(USER_ID, request);

        assertNotNull(response);
        assertEquals(CONVERSATION_ID, response.getConversationId());
        assertEquals("Give me a workout plan", response.getTitle());
        assertEquals("Start with a 20-minute walk.", response.getReply());
        assertEquals(2, response.getSuggestions().size());

        verify(geminiService).getChatAnswer(anyList());
        verify(geminiService).getChatSuggestions(anyList(), anyString());
        verify(conversationRepository).save(any(ChatConversation.class));
    }

    @Test
    void sendMessageShouldRejectNullMessage() {
        ChatRequest request = createRequest(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(USER_ID, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(geminiService);
        verify(conversationRepository, never())
                .save(any(ChatConversation.class));
    }

    @Test
    void sendMessageShouldRejectBlankMessage() {
        ChatRequest request = createRequest("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(USER_ID, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(geminiService);
    }

    @Test
    void sendMessageShouldRejectMessageLongerThan2000Characters() {
        ChatRequest request = createRequest("a".repeat(2001));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(USER_ID, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(geminiService);
    }

    @Test
    void sendMessageShouldReturnNotFoundForAnotherUsersConversation() {
        ChatRequest request = createRequest("Continue my workout");

        request.setConversationId(CONVERSATION_ID);

        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.sendMessage(USER_ID, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(geminiService);
    }

    @Test
    void sendMessageShouldAppendMessagesToExistingConversation() {
        mockContext();

        ChatConversation conversation = createConversation();

        conversation.getMessages().add(
                ChatConversation.ChatMessage.builder()
                        .role("user")
                        .content("I want to improve my stamina")
                        .build()
        );

        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.of(conversation));

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn("Try adding light cardio.");

        when(geminiService.getChatSuggestions(anyList(), anyString()))
                .thenReturn(List.of("How many days per week?"));

        when(conversationRepository.save(any(ChatConversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChatRequest request = createRequest("Suggest some cardio exercises");
        request.setConversationId(CONVERSATION_ID);

        ChatResponse response = chatService.sendMessage(USER_ID, request);

        assertEquals("Existing conversation", response.getTitle());
        assertEquals(3, conversation.getMessages().size());
        assertEquals(
                "Suggest some cardio exercises",
                conversation.getMessages().get(1).getContent()
        );
        assertEquals(
                "Try adding light cardio.",
                conversation.getMessages().get(2).getContent()
        );
    }

    @Test
    void sendMessageShouldRejectEmptyAiResponse() {
        mockContext();

        when(geminiService.getChatAnswer(anyList()))
                .thenReturn(" ");

        ChatRequest request = createRequest("Give me fitness advice");

        assertThrows(
                IllegalStateException.class,
                () -> chatService.sendMessage(USER_ID, request)
        );

        verify(conversationRepository, never())
                .save(any(ChatConversation.class));
    }

    @Test
    void getConversationsShouldReturnUserConversations() {
        List<ChatConversation> conversations = List.of(
                createConversation()
        );

        when(conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(USER_ID))
                .thenReturn(conversations);

        List<ChatConversation> result =
                chatService.getConversations(USER_ID);

        assertEquals(1, result.size());
        assertEquals(CONVERSATION_ID, result.get(0).getId());

        verify(conversationRepository)
                .findByUserIdOrderByUpdatedAtDesc(USER_ID);
    }

    @Test
    void getConversationShouldReturnConversationForOwner() {
        ChatConversation conversation = createConversation();

        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.of(conversation));

        ChatConversation result =
                chatService.getConversation(USER_ID, CONVERSATION_ID);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
    }

    @Test
    void getConversationShouldRejectUnauthorizedUser() {
        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.getConversation(USER_ID, CONVERSATION_ID)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteConversationShouldDeleteUsersConversation() {
        ChatConversation conversation = createConversation();

        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.of(conversation));

        chatService.deleteConversation(USER_ID, CONVERSATION_ID);

        verify(conversationRepository).delete(conversation);
    }

    @Test
    void editMessageShouldRejectInvalidMessageIndex() {
        ChatConversation conversation = createConversation();

        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.of(conversation));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.editMessageAndStream(
                        USER_ID,
                        CONVERSATION_ID,
                        0,
                        createRequest("Updated message")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void regenerateResponseShouldRejectInvalidMessageIndex() {
        ChatConversation conversation = createConversation();

        when(conversationRepository.findByIdAndUserId(
                CONVERSATION_ID, USER_ID
        )).thenReturn(Optional.of(conversation));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.regenerateResponseAndStream(
                        USER_ID,
                        CONVERSATION_ID,
                        0
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void streamMessageShouldRejectBlankMessage() {
        ChatRequest request = createRequest(" ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chatService.streamMessage(USER_ID, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(geminiService);
    }
}
