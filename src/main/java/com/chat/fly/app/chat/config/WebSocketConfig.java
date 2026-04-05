package com.chat.fly.app.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.chat.fly.app.chat.websocket.StompHandshakeInterceptor;
import com.chat.fly.app.chat.websocket.WebSocketPrincipalHandshakeHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatProperties chatProperties;
    private final StompHandshakeInterceptor stompHandshakeInterceptor;


    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker(
                chatProperties.getWebsocket().getTopicPrefix(),
                chatProperties.getWebsocket().getQueuePrefix());
        config.setUserDestinationPrefix(chatProperties.getWebsocket().getUserDestinationPrefix());
        config.setApplicationDestinationPrefixes(chatProperties.getWebsocket().getApplicationDestinationPrefix());
    }

    @Override
    @SuppressWarnings("null") // List<String> elements are guaranteed non-null by @NotEmpty; TYPE_USE annotations not supported by Spring's @NonNull
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(chatProperties.getWebsocket().getEndpoint())
                .addInterceptors(stompHandshakeInterceptor)
                .setHandshakeHandler(new WebSocketPrincipalHandshakeHandler())
                .setAllowedOriginPatterns(chatProperties.getWebsocket().getAllowedOriginPatterns().toArray(new String[0]))
                .withSockJS();
    }
}
