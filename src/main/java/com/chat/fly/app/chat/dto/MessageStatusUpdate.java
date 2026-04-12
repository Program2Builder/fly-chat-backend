package com.chat.fly.app.chat.dto;

import com.chat.fly.app.chat.model.MessageStatus;

public record MessageStatusUpdate(String messageId, MessageStatus status) {
}
