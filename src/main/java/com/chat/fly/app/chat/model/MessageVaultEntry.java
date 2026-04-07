package com.chat.fly.app.chat.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for the Message Vault.
 *
 * Stores AES-GCM ciphertexts produced by the client.
 * The server is zero-knowledge — ciphertext is never decrypted or logged here.
 */
@Document(collection = "message_vault")
@CompoundIndexes({
    // Enforce one vault entry per user per message (acts as unique constraint)
    @CompoundIndex(name = "uq_vault_user_message", def = "{'userId': 1, 'messageId': 1}", unique = true),
    // Efficient paginated listing ordered by createdAt DESC
    @CompoundIndex(name = "idx_vault_user_created", def = "{'userId': 1, 'createdAt': -1}")
})
public class MessageVaultEntry {

    @Id
    private String id;

    private Long userId;

    private String messageId;

    // Treat as sensitive — never log this field
    private String ciphertext;

    private Instant createdAt;

    public MessageVaultEntry() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getCiphertext() { return ciphertext; }
    public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
