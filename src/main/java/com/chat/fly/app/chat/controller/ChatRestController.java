package com.chat.fly.app.chat.controller;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.chat.fly.app.chat.model.ChatMessage;
import com.chat.fly.app.chat.model.Media;
import com.chat.fly.app.chat.dto.ChatContactResponse;
import com.chat.fly.app.chat.dto.ChatBootstrapResponse;
import com.chat.fly.app.chat.dto.ChatGroupResponse;
import com.chat.fly.app.chat.dto.CreateChatGroupRequest;
import com.chat.fly.app.chat.dto.KeyBundleResponse;
import com.chat.fly.app.chat.dto.KeyRegistrationRequest;
import com.chat.fly.app.chat.dto.MediaUploadResponse;
import com.chat.fly.app.chat.service.ChatService;
import com.chat.fly.app.chat.service.MediaStorageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final MediaStorageService mediaStorageService;
    private static final Logger log = LoggerFactory.getLogger(ChatRestController.class);


    @PostMapping("/media")
    public MediaUploadResponse uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
        Media media = chatService.saveMedia(mediaStorageService.store(file));
        return new MediaUploadResponse(
                media.getId(),
                media.getStoredFilename(),
                "/api/chat/media/" + media.getId() + "/download",
                media.getContentType(),
                media.getSize());
    }

    @GetMapping("/media/{id}/download")
    public ResponseEntity<Resource> downloadMedia(@PathVariable @NonNull String id) throws Exception {
        Media media = chatService.findMedia(id);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }

        Path file = mediaStorageService.resolveStoredFile(media);
        if (!java.nio.file.Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stored file was not found");
        }

        URI fileUri = Objects.requireNonNull(file.toUri(), "File URI must not be null");
        Resource resource = new UrlResource(fileUri);
        String contentType = media.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : media.getContentType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Objects.requireNonNull(contentType)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + media.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/history/room/{roomId}")
    public Page<ChatMessage> getRoomHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatService.getMessagesForRoom(roomId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @GetMapping("/history/direct/{targetUsername}")
    public Page<ChatMessage> getDirectHistory(
            Principal principal,
            @PathVariable String targetUsername,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatService.getDirectHistory(principal.getName(), targetUsername,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @GetMapping("/bootstrap")
    public ChatBootstrapResponse getBootstrap(Principal principal) {
        return chatService.getBootstrap(principal.getName());
    }

    @GetMapping("/contacts")
    public List<ChatContactResponse> getContacts(Principal principal) {
        return chatService.getContactsForUser(principal.getName());
    }

    @GetMapping("/groups")
    public List<ChatGroupResponse> getGroups(Principal principal) {
        return chatService.getGroupsForUser(principal.getName());
    }

    @PostMapping("/groups")
    public ChatGroupResponse createGroup(Principal principal, @Valid @RequestBody CreateChatGroupRequest request) {
        return chatService.createGroup(principal.getName(), request);
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(Principal principal, @PathVariable Long groupId) {
        chatService.deleteGroup(principal.getName(), groupId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/{groupId}/members/{username}")
    public ResponseEntity<Void> addMemberToGroup(Principal principal, @PathVariable Long groupId, @PathVariable String username) {
        chatService.addMemberToGroup(principal.getName(), groupId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/groups/{groupId}/members/{username}")
    public ResponseEntity<Void> removeMemberFromGroup(Principal principal, @PathVariable Long groupId, @PathVariable String username) {
        chatService.removeMemberFromGroup(principal.getName(), groupId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contacts/{username}")
    public ResponseEntity<Void> addContact(Principal principal, @PathVariable String username) {
        chatService.addContact(principal.getName(), username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/contacts/{username}")
    public ResponseEntity<Void> removeContact(Principal principal, @PathVariable String username) {
        chatService.removeContact(principal.getName(), username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/picture")
    public ResponseEntity<Void> updateProfilePicture(Principal principal, @RequestParam("file") MultipartFile file)
            throws IOException {
        chatService.updateProfilePicture(principal.getName(), file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/keys")
    public ResponseEntity<Void> registerKeys(Principal principal, @RequestBody KeyRegistrationRequest request) {
        chatService.registerKeys(principal.getName(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/keys/{username}")
    public KeyBundleResponse getKeyBundle(@PathVariable String username) {
        return chatService.getKeyBundle(username);
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException exception) {
        log.debug("Rejected request: {}", exception.getMessage());
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
