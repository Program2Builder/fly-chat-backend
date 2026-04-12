package com.chat.fly.app.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VaultStoreRequest(

    @NotBlank(message = "messageId must not be blank")
    @Size(max = 255, message = "messageId must not exceed 255 characters")
    String messageId,
    
    @NotBlank(message = "ciphertext must not be blank")
    @Size(max = 8192, message = "ciphertext must not exceed 8192 characters")
    String ciphertext,

    String senderId,
    String receiverId
) {}
