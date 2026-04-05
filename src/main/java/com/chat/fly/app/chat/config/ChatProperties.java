package com.chat.fly.app.chat.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties(prefix = "chat")
public class ChatProperties {

    private final Websocket websocket = new Websocket();
    private final Media media = new Media();

    public Websocket getWebsocket() {
        return websocket;
    }

    public Media getMedia() {
        return media;
    }

    public static class Websocket {

        @NotBlank
        private @NonNull String endpoint = "/ws";

        @NotBlank
        private @NonNull String applicationDestinationPrefix = "/app";

        @NotBlank
        private @NonNull String userDestinationPrefix = "/user";

        @NotBlank
        private @NonNull String topicPrefix = "/topic";

        @NotBlank
        private @NonNull String queuePrefix = "/queue";

        @NotEmpty
        private @NonNull List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));

        public @NonNull String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(@NonNull String endpoint) {
            this.endpoint = endpoint;
        }

        public @NonNull String getApplicationDestinationPrefix() {
            return applicationDestinationPrefix;
        }

        public void setApplicationDestinationPrefix(@NonNull String applicationDestinationPrefix) {
            this.applicationDestinationPrefix = applicationDestinationPrefix;
        }

        public @NonNull String getUserDestinationPrefix() {
            return userDestinationPrefix;
        }

        public void setUserDestinationPrefix(@NonNull String userDestinationPrefix) {
            this.userDestinationPrefix = userDestinationPrefix;
        }

        public @NonNull String getTopicPrefix() {
            return topicPrefix;
        }

        public void setTopicPrefix(@NonNull String topicPrefix) {
            this.topicPrefix = topicPrefix;
        }

        public @NonNull String getQueuePrefix() {
            return queuePrefix;
        }

        public void setQueuePrefix(@NonNull String queuePrefix) {
            this.queuePrefix = queuePrefix;
        }

        public @NonNull List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(@NonNull List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }
    }

    public static class Media {

        @NotBlank
        private String uploadDir = "uploads";

        @Min(1)
        private long maxFileSizeBytes = 10485760L;

        @NotEmpty
        private List<String> allowedContentTypes = new ArrayList<>(List.of(
                "image/jpeg",
                "image/png",
                "image/webp",
                "application/pdf"));

        public Path getUploadDirPath() {
            return Path.of(uploadDir).normalize();
        }

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }

        public long getMaxFileSizeBytes() {
            return maxFileSizeBytes;
        }

        public void setMaxFileSizeBytes(long maxFileSizeBytes) {
            this.maxFileSizeBytes = maxFileSizeBytes;
        }

        public List<String> getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(List<String> allowedContentTypes) {
            this.allowedContentTypes = allowedContentTypes;
        }
    }
}
