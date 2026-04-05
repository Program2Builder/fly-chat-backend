package com.chat.fly.app.chat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class ChatWebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketEventListener.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WS CONNECTED | sessionId={} | user={}", sha.getSessionId(),
                sha.getUser() != null ? sha.getUser().getName() : "anonymous");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WS DISCONNECTED | sessionId={} | user={}", sha.getSessionId(),
                sha.getUser() != null ? sha.getUser().getName() : "anonymous");
    }
}
