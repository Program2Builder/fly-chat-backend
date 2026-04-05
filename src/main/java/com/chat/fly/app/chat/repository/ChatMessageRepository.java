package com.chat.fly.app.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.chat.fly.app.chat.model.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Page<ChatMessage> findByRoomId(String roomId, Pageable pageable);

    @Query("{ '$or': [ " +
           "{ 'senderId': ?0, 'recipients': ?1, 'roomId': null }, " +
           "{ 'senderId': ?1, 'recipients': ?0, 'roomId': null } " +
           "] }")
    Page<ChatMessage> findDirectMessages(String userA, String userB, Pageable pageable);
}
