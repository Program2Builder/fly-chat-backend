package com.chat.fly.app.chat.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.chat.fly.app.chat.config.ChatProperties;
import com.chat.fly.app.chat.model.Media;

@Service
public class MediaStorageService {

    private final ChatProperties chatProperties;
    private final Path uploadRoot;

    public MediaStorageService(ChatProperties chatProperties) throws IOException {
        this.chatProperties = chatProperties;
        this.uploadRoot = chatProperties.getMedia().getUploadDirPath().toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public Media store(MultipartFile file) throws IOException {
        validate(file);

        String rawName = file.getOriginalFilename();
        String originalFilename = StringUtils.cleanPath(rawName != null ? rawName : "");
        String storedFilename = UUID.randomUUID() + "-" + originalFilename;
        Path targetFile = uploadRoot.resolve(storedFilename).normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid upload path");
        }

        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        Media media = new Media();
        media.setOriginalFilename(originalFilename);
        media.setStoredFilename(storedFilename);
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setPath(targetFile.toString());
        return media;
    }

    public Path resolveStoredFile(Media media) {
        Path storedFile = Path.of(media.getPath()).toAbsolutePath().normalize();
        if (!storedFile.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Stored file is outside the configured upload directory");
        }
        return storedFile;
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String rawFilename = file.getOriginalFilename();
        String filename = StringUtils.cleanPath(rawFilename != null ? rawFilename : "");
        if (!StringUtils.hasText(filename) || filename.contains("..")) {
            throw new IllegalArgumentException("Uploaded file name is invalid");
        }

        if (file.getSize() > chatProperties.getMedia().getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("Uploaded file exceeds the configured size limit");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        if (!chatProperties.getMedia().getAllowedContentTypes().contains(contentType)) {
            throw new IllegalArgumentException("Uploaded file content type is not allowed");
        }
    }
}
