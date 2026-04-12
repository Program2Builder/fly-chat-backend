package com.chat.fly.app.chat.service;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


import com.chat.fly.app.auth.model.AppUser;
import com.chat.fly.app.auth.repository.AppUserRepository;
import com.chat.fly.app.chat.dto.VaultEntryResponse;
import com.chat.fly.app.chat.dto.VaultPageResponse;
import com.chat.fly.app.chat.dto.VaultStoreRequest;
import com.chat.fly.app.chat.model.MessageVaultEntry;
import com.chat.fly.app.chat.repository.MessageVaultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageVaultService {

    private final MessageVaultRepository vaultRepository;
    private final AppUserRepository userRepository;

    /**
     * Upsert a single vault entry for the given user.
     * The ciphertext is never logged (zero-knowledge storage).
     */
    public VaultEntryResponse storeEntry(String userId, String messageId, String ciphertext, String senderId, String receiverId) {
        MessageVaultEntry entry = vaultRepository
                .findByUserIdAndMessageId(userId, messageId)
                .orElseGet(() -> {
                    MessageVaultEntry e = new MessageVaultEntry();
                    e.setUserId(userId);
                    e.setMessageId(messageId);
                    e.setCreatedAt(Instant.now());
                    return e;
                });

        entry.setCiphertext(ciphertext);
        entry.setSenderId(senderId);
        entry.setReceiverId(receiverId);
        MessageVaultEntry saved = vaultRepository.save(entry);
        return toResponse(saved);
    }

    /**
     * Batch upsert vault entries. Returns the count of saved entries.
     */
    public int storeEntries(String userId, List<VaultStoreRequest> entries) {
        for (VaultStoreRequest req : entries) {
            storeEntry(userId, req.messageId(), req.ciphertext(), req.senderId(), req.receiverId());
        }
        return entries.size();
    }

    /**
     * Return a paginated list of vault entries for the authenticated user.
     * Searches both numeric userId and username.
     */
    public VaultPageResponse getEntries(String userId, String username, Pageable pageable) {
        List<MessageVaultEntry> page = vaultRepository
                .findAllByUserIdOrUsername(userId, username, pageable);

        long total = vaultRepository.countAllByUserIdOrUsername(userId, username);
        long fetched = (long) pageable.getPageNumber() * pageable.getPageSize() + page.size();
        boolean hasMore = fetched < total;

        List<VaultEntryResponse> responses = page.stream()
                .map(this::toResponse)
                .toList();

        return new VaultPageResponse(responses, hasMore, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * Return a paginated list of vault entries for a specific direct conversation.
     * Searches by both numeric userId and username to ensure legacy recovery.
     */
    public VaultPageResponse getDirectEntries(String userId, String username, String targetUsername, Pageable pageable) {
        List<MessageVaultEntry> page = vaultRepository
                .findDirectConversation(userId, username, targetUsername, pageable);

        long total = vaultRepository.countDirectConversation(userId, username, targetUsername);
        long fetched = (long) pageable.getPageNumber() * pageable.getPageSize() + page.size();
        boolean hasMore = fetched < total;

        List<VaultEntryResponse> responses = page.stream()
                .map(this::toResponse)
                .toList();

        return new VaultPageResponse(responses, hasMore, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * Fetch a single vault entry for the given user.
     */
    public VaultEntryResponse getEntry(String userId, String messageId) {
        log.info("Fetching vault entry for userId: {}, messageId: {}", userId, messageId);
        return vaultRepository.findByUserIdAndMessageId(userId, messageId)
                .map(this::toResponse)
                .orElseThrow(() -> {
                    log.warn("Vault entry NOT FOUND for userId: {}, messageId: {}", userId, messageId);
                    return new IllegalArgumentException("Vault entry not found for message: " + messageId);
                });
    }

    /**
     * Delete a vault entry by userId + messageId (idempotent — no error if not
     * found).
     */
    public void deleteEntry(String userId, String messageId) {
        vaultRepository.deleteByUserIdAndMessageId(userId, messageId);
    }

    /**
     * Resolve the authenticated user's ID from their username.
     */
    public Long resolveUserId(String username) {
        return userRepository.findByUsername(username)
                .map(AppUser::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private VaultEntryResponse toResponse(MessageVaultEntry entry) {
        return new VaultEntryResponse(
                entry.getMessageId(),
                entry.getCiphertext(), // returned to the owner only
                entry.getCreatedAt().toString() // ISO-8601
        );
    }
}
