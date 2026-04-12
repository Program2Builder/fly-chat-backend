package com.chat.fly.app.chat.dto;

import java.time.Instant;

public record ChatContactResponse(
        String id,
        String displayName,
        Instant lastInteractionAt,
        String profilePictureUrl) {
}
