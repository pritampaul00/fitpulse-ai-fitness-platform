package com.fitness.aiservice.repository;

import com.fitness.aiservice.model.ChatConversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository
        extends MongoRepository<ChatConversation, String> {

    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(
            String userId
    );

    Optional<ChatConversation> findByIdAndUserId(
            String id,
            String userId
    );
}
