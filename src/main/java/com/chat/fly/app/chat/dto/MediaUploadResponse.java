package com.chat.fly.app.chat.dto;

public record MediaUploadResponse(
        String id,
        String storedFilename,
        String downloadUrl,
        String contentType,
        long size) {
}
