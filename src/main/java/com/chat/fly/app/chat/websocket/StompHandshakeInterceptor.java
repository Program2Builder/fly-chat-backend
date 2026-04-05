package com.chat.fly.app.chat.websocket;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class StompHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        String username = request.getHeaders().getFirst("X-User-Id");

        if (!StringUtils.hasText(username)) {
            username = UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("username");
        }

        final String principalName = Objects.requireNonNullElse(username, "anonymous-" + System.currentTimeMillis()).trim();
        attributes.put("username", principalName);
        attributes.put("principal", new Principal() {
            @Override
            public String getName() {
                return principalName;
            }
        });

        log.info("WS HANDSHAKE {} | origin={} | user={}",
                request.getURI().getPath(),
                request.getHeaders().getOrigin(),
                principalName);
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        if (exception != null) {
            log.warn("WS HANDSHAKE FAILED {} | {}", request.getURI().getPath(), exception.getMessage());
        }
    }
}
