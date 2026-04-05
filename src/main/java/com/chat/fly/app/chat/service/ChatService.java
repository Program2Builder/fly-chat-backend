package com.chat.fly.app.chat.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.chat.fly.app.auth.dto.AuthResponse;
import com.chat.fly.app.auth.model.AppUser;
import com.chat.fly.app.auth.repository.AppUserRepository;
import com.chat.fly.app.chat.dto.ChatBootstrapResponse;
import com.chat.fly.app.chat.dto.ChatContactResponse;
import com.chat.fly.app.chat.dto.ChatGroupResponse;
import com.chat.fly.app.chat.dto.CreateChatGroupRequest;
import com.chat.fly.app.chat.model.ChatGroup;
import com.chat.fly.app.chat.model.ChatMessage;
import com.chat.fly.app.chat.model.Media;
import com.chat.fly.app.chat.model.MessageType;
import com.chat.fly.app.chat.repository.ChatGroupRepository;
import com.chat.fly.app.chat.repository.ChatMessageRepository;
import com.chat.fly.app.chat.repository.MediaRepository;
import com.chat.fly.app.auth.model.UserKeyStore;
import com.chat.fly.app.auth.repository.UserKeyRepository;
import com.chat.fly.app.chat.dto.KeyBundleResponse;
import com.chat.fly.app.chat.dto.KeyRegistrationRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository messageRepo;
    private final MediaRepository mediaRepo;
    private final AppUserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final MediaStorageService mediaStorageService;
    private final UserKeyRepository userKeyRepository;

    public ChatMessage saveMessage(ChatMessage msg) {
        validateMessage(msg);
        normalizeMessage(msg);

        if (msg.getTimestamp() == null) {
            msg.setTimestamp(Instant.now());
        }
        return messageRepo.save(msg);
    }

    public Page<ChatMessage> getMessagesForRoom(String roomId, Pageable pageable) {
        return messageRepo.findByRoomId(roomId, pageable);
    }

    public Page<ChatMessage> getDirectHistory(String userA, String userB, Pageable pageable) {
        return messageRepo.findDirectMessages(userA, userB, pageable);
    }

    public List<ChatContactResponse> getContactsForUser(String username) {
        AppUser currentUser = getUserByUsername(username);
        Map<String, Instant> lastInteractionByUser = resolveLastInteractions(currentUser.getUsername());

        return currentUser.getContacts().stream()
                .map(user -> new ChatContactResponse(
                        user.getUsername(),
                        user.getDisplayName(),
                        lastInteractionByUser.get(user.getUsername())))
                .sorted(Comparator.comparing(ChatContactResponse::lastInteractionAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ChatContactResponse::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void addContact(String ownerUsername, String contactUsername) {
        if (ownerUsername.equalsIgnoreCase(contactUsername)) {
            throw new IllegalArgumentException("Cannot add yourself as a contact");
        }
        AppUser owner = getUserByUsername(ownerUsername);
        AppUser contact = getUserByUsername(contactUsername);
        owner.getContacts().add(contact);
        contact.getContacts().add(owner);
        userRepository.saveAll(List.of(owner, contact));
    }

    public void removeContact(String ownerUsername, String contactUsername) {
        AppUser owner = getUserByUsername(ownerUsername);
        AppUser contact = getUserByUsername(contactUsername);
        owner.getContacts().remove(contact);
        contact.getContacts().remove(owner);
        userRepository.saveAll(List.of(owner, contact));
    }

    public List<ChatGroupResponse> getGroupsForUser(String username) {
        AppUser currentUser = getUserByUsername(username);
        ensureDefaultGroupsForUser(currentUser);

        return chatGroupRepository.findDistinctByMembersUsernameOrderByNameAsc(currentUser.getUsername()).stream()
                .map(group -> new ChatGroupResponse(
                        group.getId(),
                        group.getRoomId(),
                        group.getName(),
                        group.getDescription(),
                        group.isAutoJoin()))
                .toList();
    }

    public ChatBootstrapResponse getBootstrap(String username) {
        AppUser user = getUserByUsername(username);
        return new ChatBootstrapResponse(
                toUserProfileResponse(user),
                getContactsForUser(user.getUsername()),
                getGroupsForUser(user.getUsername()));
    }

    public AuthResponse.UserProfileResponse toUserProfileResponse(AppUser user) {
        return new AuthResponse.UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRoles(),
                toProfilePictureUrl(user.getProfilePictureId()));
    }

    public ChatGroupResponse createGroup(String username, CreateChatGroupRequest request) {
        AppUser creator = getUserByUsername(username);
        String baseRoomId = toRoomId(request.name());
        String roomId = baseRoomId;
        int suffix = 2;
        while (chatGroupRepository.findByRoomId(roomId).isPresent()) {
            roomId = baseRoomId + "-" + suffix++;
        }

        ChatGroup group = new ChatGroup();
        group.setRoomId(roomId);
        group.setName(request.name().trim());
        group.setDescription(request.description() != null ? request.description().trim() : null);
        group.setAutoJoin(false);
        group.setCreator(creator);
        group.getMembers().add(creator);

        if (request.memberUsernames() != null) {
            List<AppUser> members = request.memberUsernames().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(memberUsername -> !memberUsername.equals(creator.getUsername()))
                    .map(this::getUserByUsername)
                    .filter(Objects::nonNull)
                    .filter(member -> creator.getContacts().contains(member))
                    .toList();
            group.getMembers().addAll(members);
        }

        ChatGroup saved = chatGroupRepository.save(group);
        return new ChatGroupResponse(
                saved.getId(),
                saved.getRoomId(),
                saved.getName(),
                saved.getDescription(),
                saved.isAutoJoin());
    }

    public void deleteGroup(String username, long groupId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (group.getCreator() == null || !group.getCreator().getUsername().equals(username)) {
            throw new IllegalArgumentException("Only the group creator can delete this group");
        }

        chatGroupRepository.delete(group);
    }

    public void addMemberToGroup(String requesterUsername, long groupId, String targetUsername) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (group.getCreator() == null || !group.getCreator().getUsername().equals(requesterUsername)) {
            throw new IllegalArgumentException("Only the group creator can add members");
        }

        AppUser targetUser = getUserByUsername(targetUsername);
        if (group.getMembers().stream().anyMatch(m -> m.getUsername().equals(targetUser.getUsername()))) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        group.getMembers().add(targetUser);
        chatGroupRepository.save(group);
    }

    public void removeMemberFromGroup(String requesterUsername, long groupId, String targetUsername) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Allow both the creator to remove anyone, and users to remove themselves (Leave Group)
        boolean isCreator = group.getCreator() != null && group.getCreator().getUsername().equals(requesterUsername);
        boolean isSelf = requesterUsername.equals(targetUsername);

        if (!isCreator && !isSelf) {
            throw new IllegalArgumentException("not authorized to remove this member");
        }

        AppUser targetUser = getUserByUsername(targetUsername);
        
        if (isCreator && targetUsername.equals(group.getCreator().getUsername())) {
             throw new IllegalArgumentException("Creator cannot remove themselves. Delete the group instead.");
        }

        group.getMembers().removeIf(m -> m.getUsername().equals(targetUser.getUsername()));
        chatGroupRepository.save(group);
    }

    public void ensureDefaultGroupsForUser(AppUser user) {
        ChatGroup generalGroup = chatGroupRepository.findByRoomId("general")
                .orElseGet(() -> {
                    ChatGroup group = new ChatGroup();
                    group.setRoomId("general");
                    group.setName("General");
                    group.setDescription("Default group for all users");
                    group.setAutoJoin(true);
                    return chatGroupRepository.save(group);
                });

        if (generalGroup.getMembers().stream().noneMatch(member -> member.getId().equals(user.getId()))) {
            generalGroup.getMembers().add(user);
            chatGroupRepository.save(generalGroup);
        }
    }

    public Media saveMedia(Media media) {
        media.setUploadedAt(Instant.now());
        return mediaRepo.save(media);
    }

    public void updateProfilePicture(String username, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        AppUser user = getUserByUsername(username);
        
        // Use existing media storage service
        com.chat.fly.app.chat.model.Media media = saveMedia(mediaStorageService.store(file));
        user.setProfilePictureId(media.getId());
        userRepository.save(user);
    }

    private String toProfilePictureUrl(String profilePictureId) {
        if (!StringUtils.hasText(profilePictureId)) {
            return null;
        }
        return "/api/chat/media/" + profilePictureId + "/download";
    }

    public Media findMedia(String id) {
        return mediaRepo.findById(id).orElse(null);
    }

    private void validateMessage(ChatMessage msg) {
        if (msg.getType() == null) {
            throw new IllegalArgumentException("Message type is required");
        }

        if (!StringUtils.hasText(msg.getSenderId())) {
            throw new IllegalArgumentException("Sender id is required");
        }

        if (msg.getType() == MessageType.TEXT && !StringUtils.hasText(msg.getContent())) {
            throw new IllegalArgumentException("Text messages require content");
        }

        if (msg.getType() == MessageType.MEDIA && !StringUtils.hasText(msg.getMediaId())) {
            throw new IllegalArgumentException("Media messages require a media id");
        }

        boolean hasRoom = StringUtils.hasText(msg.getRoomId());
        boolean hasRecipients = !CollectionUtils.isEmpty(msg.getRecipients());
        if (!hasRoom && !hasRecipients) {
            throw new IllegalArgumentException("A room id or recipients list is required");
        }
    }

    private void normalizeMessage(ChatMessage msg) {
        if (msg.getRecipients() != null) {
            msg.setRecipients(msg.getRecipients().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList()));
        }

        if (StringUtils.hasText(msg.getRoomId())) {
            msg.setRoomId(msg.getRoomId().trim());
        }

        if (StringUtils.hasText(msg.getSenderId())) {
            msg.setSenderId(msg.getSenderId().trim());
        }

        if (StringUtils.hasText(msg.getSenderName())) {
            msg.setSenderName(msg.getSenderName().trim());
        }
    }

    private Map<String, Instant> resolveLastInteractions(String currentUserId) {
        Map<String, Instant> interactions = new LinkedHashMap<>();
        for (ChatMessage message : messageRepo.findAll()) {
            collectInteractionFromSender(currentUserId, message, interactions);
            collectInteractionFromRecipients(currentUserId, message, interactions);
        }
        return interactions;
    }

    private void collectInteractionFromSender(String currentUserId, ChatMessage message,
            Map<String, Instant> interactions) {
        if (!StringUtils.hasText(message.getSenderId())) {
            return;
        }

        String senderId = message.getSenderId().trim();
        if (currentUserId.equals(senderId)) {
            return;
        }

        if (isRelevantToUser(currentUserId, message)) {
            upsertInteraction(interactions, senderId, message.getTimestamp());
        }
    }

    private void collectInteractionFromRecipients(String currentUserId, ChatMessage message,
            Map<String, Instant> interactions) {
        if (CollectionUtils.isEmpty(message.getRecipients())) {
            return;
        }

        boolean sentByCurrentUser = currentUserId.equals(message.getSenderId());
        boolean addressedToCurrentUser = message.getRecipients().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch(currentUserId::equals);

        if (!sentByCurrentUser && !addressedToCurrentUser) {
            return;
        }

        for (String recipient : message.getRecipients()) {
            if (!StringUtils.hasText(recipient)) {
                continue;
            }

            String recipientId = recipient.trim();
            if (currentUserId.equals(recipientId)) {
                continue;
            }

            upsertInteraction(interactions, recipientId, message.getTimestamp());
        }
    }

    private boolean isRelevantToUser(String currentUserId, ChatMessage message) {
        if (CollectionUtils.isEmpty(message.getRecipients())) {
            return false;
        }

        boolean sentByCurrentUser = currentUserId.equals(message.getSenderId());
        boolean addressedToCurrentUser = message.getRecipients().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch(currentUserId::equals);
        return sentByCurrentUser || addressedToCurrentUser;
    }

    private void upsertInteraction(Map<String, Instant> interactions, String contactId, Instant interactionAt) {
        Instant current = interactions.get(contactId);
        if (current == null || isAfter(interactionAt, current)) {
            interactions.put(contactId, interactionAt);
        }
    }

    private boolean isAfter(Instant candidate, Instant existing) {
        if (candidate == null) {
            return false;
        }
        if (existing == null) {
            return true;
        }
        return candidate.isAfter(existing);
    }

    private AppUser getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void registerKeys(String username, KeyRegistrationRequest request) {
        AppUser user = getUserByUsername(username);
        UserKeyStore store = userKeyRepository.findByUser(user).orElse(new UserKeyStore());
        store.setUser(user);
        store.setRegistrationId(request.registrationId());
        store.setIdentityPublicKey(request.identityPublicKey());
        
        if (request.signedPreKey() != null) {
            store.setSignedPreKeyId(request.signedPreKey().keyId());
            store.setSignedPreKey(request.signedPreKey().publicKey());
            store.setSignedPreKeySignature(request.signedPreKey().signature());
        }

        store.getOneTimePreKeys().clear();
        if (request.oneTimePreKeys() != null) {
            for (KeyRegistrationRequest.OneTimePreKeyRequest opk : request.oneTimePreKeys()) {
                store.getOneTimePreKeys().put(opk.keyId(), opk.publicKey());
            }
        }
        userKeyRepository.save(store);
    }

    public KeyBundleResponse getKeyBundle(String targetUsername) {
        AppUser target = getUserByUsername(targetUsername);
        UserKeyStore store = userKeyRepository.findByUser(target)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "No encryption keys registered for this user"));

        KeyBundleResponse.PreKeyResponse oneTimePreKey = null;
        if (!store.getOneTimePreKeys().isEmpty()) {
            // Consume one key to prevent reuse
            Integer keyId = store.getOneTimePreKeys().keySet().iterator().next();
            String publicKey = store.getOneTimePreKeys().remove(keyId);
            userKeyRepository.save(store);
            oneTimePreKey = new KeyBundleResponse.PreKeyResponse(keyId, publicKey, null);
        }

        return new KeyBundleResponse(
                target.getUsername(),
                store.getRegistrationId(),
                store.getIdentityPublicKey(),
                new KeyBundleResponse.PreKeyResponse(
                        store.getSignedPreKeyId(),
                        store.getSignedPreKey(),
                        store.getSignedPreKeySignature()),
                oneTimePreKey);
    }

    private String toRoomId(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("Group name must contain letters or numbers");
        }
        return normalized;
    }
}
