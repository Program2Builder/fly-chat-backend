package com.chat.fly.app.chat.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatGroupRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 255) String description,
        List<String> memberUsernames) {
}
