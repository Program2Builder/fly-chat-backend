package com.chat.fly.app.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.chat.fly.app.chat.model.MessageVaultEntry;

@Repository
public interface MessageVaultRepository extends MongoRepository<MessageVaultEntry, String> {

    Optional<MessageVaultEntry> findByUserIdAndMessageId(Long userId, String messageId);

    List<MessageVaultEntry> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndMessageId(Long userId, String messageId);

    long countByUserId(Long userId);
}
