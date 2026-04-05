package com.chat.fly.app.chat.websocket;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.chat.fly.app.chat.config.ChatProperties;
import com.chat.fly.app.chat.model.ChatMessage;
import com.chat.fly.app.chat.model.MessageType;
import com.chat.fly.app.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatProperties chatProperties;
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);


    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message, Principal principal) {
        applyAuthenticatedSender(message, principal);
        message.setTimestamp(Instant.now());

        if (message.getType() == MessageType.TYPING) {
            routeTypingMessage(message);
            return;
        }

        ChatMessage saved = chatService.saveMessage(message);

        if (saved.getRoomId() != null && !saved.getRoomId().isEmpty()) {
            messagingTemplate.convertAndSend(chatProperties.getWebsocket().getTopicPrefix() + "/room." + saved.getRoomId(), saved);
        } else if (saved.getRecipients() != null) {
            saved.getRecipients().forEach(recipient -> {
                messagingTemplate.convertAndSendToUser(recipient, chatProperties.getWebsocket().getQueuePrefix() + "/messages", saved);
            });
        }
        log.debug("Message processed and routed: {}", saved.getId());
    }

    private void applyAuthenticatedSender(ChatMessage message, Principal principal) {
        if (principal != null && !StringUtils.hasText(message.getSenderId())) {
            message.setSenderId(principal.getName());
        }

        if (principal != null && !StringUtils.hasText(message.getSenderName())) {
            message.setSenderName(principal.getName());
        }
    }

    private void routeTypingMessage(ChatMessage message) {
        List<String> recipients = message.getRecipients();
        if (recipients != null && !recipients.isEmpty()) {
            recipients.forEach(recipient -> messagingTemplate.convertAndSendToUser(
                    recipient,
                    chatProperties.getWebsocket().getQueuePrefix() + "/messages",
                    message));
            return;
        }

        if (StringUtils.hasText(message.getRoomId())) {
            messagingTemplate.convertAndSend(
                    chatProperties.getWebsocket().getTopicPrefix() + "/room." + message.getRoomId(),
                    message);
        }
    }
}
