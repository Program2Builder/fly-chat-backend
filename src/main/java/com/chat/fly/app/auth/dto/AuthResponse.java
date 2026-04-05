package com.chat.fly.app.auth.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        UserProfileResponse user) {

    public record UserProfileResponse(
            Long id,
            String username,
            String email,
            String displayName,
            Set<String> roles,
            String profilePictureUrl) {
    }
}
