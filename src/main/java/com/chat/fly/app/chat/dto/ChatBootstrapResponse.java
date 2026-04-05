package com.chat.fly.app.chat.dto;

import java.util.List;

import com.chat.fly.app.auth.dto.AuthResponse;

public record ChatBootstrapResponse(
        AuthResponse.UserProfileResponse currentUser,
        List<ChatContactResponse> contacts,
        List<ChatGroupResponse> groups) {
}
