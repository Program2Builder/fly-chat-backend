package com.chat.fly.app.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.fly.app.chat.model.MessageVaultEntry;

@Repository
public interface MessageVaultRepository extends MongoRepository<MessageVaultEntry, String> {

    Optional<MessageVaultEntry> findByUserIdAndMessageId(String userId, String messageId);

    @org.springframework.data.mongodb.repository.Query("{ '$or': [ { 'userId': ?0 }, { 'userId': ?1 } ] }")
    List<MessageVaultEntry> findAllByUserIdOrUsername(String userId, String username, Pageable pageable);

    void deleteByUserIdAndMessageId(String userId, String messageId);

    @org.springframework.data.mongodb.repository.Query(value = "{ '$or': [ { 'userId': ?0 }, { 'userId': ?1 } ] }", count = true)
    long countAllByUserIdOrUsername(String userId, String username);

    @org.springframework.data.mongodb.repository.Query("{ '$and': [ " +
           "{ '$or': [ { 'userId': ?0 }, { 'userId': ?1 } ] }, " +
           "{ '$or': [ { 'senderId': ?1, 'receiverId': ?2 }, { 'senderId': ?2, 'receiverId': ?1 }, { 'senderId': null } ] } " +
           "] }")
    List<MessageVaultEntry> findDirectConversation(String userId, String username, String targetUsername, Pageable pageable);

    @org.springframework.data.mongodb.repository.Query(value = "{ '$and': [ " +
           "{ '$or': [ { 'userId': ?0 }, { 'userId': ?1 } ] }, " +
           "{ '$or': [ { 'senderId': ?1, 'receiverId': ?2 }, { 'senderId': ?2, 'receiverId': ?1 }, { 'senderId': null } ] } " +
           "] }", count = true)
    long countDirectConversation(String userId, String username, String targetUsername);
}
