package com.chat.fly.app.chat.dto;

public record VaultEntryResponse(
    String messageId,
    String ciphertext,
    String createdAt   // ISO-8601
) {}
