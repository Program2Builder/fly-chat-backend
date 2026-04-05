package com.chat.fly.app.chat.dto;

public record ChatGroupResponse(
        Long id,
        String roomId,
        String name,
        String description,
        boolean autoJoin) {
}
