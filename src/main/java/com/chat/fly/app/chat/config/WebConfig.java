package com.chat.fly.app.chat.config;


import java.util.Objects;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chat.fly.app.auth.security.AuthenticatedUserInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ChatProperties chatProperties;
    private final AuthenticatedUserInterceptor authenticatedUserInterceptor;
    private final RequestLoggingInterceptor requestLoggingInterceptor;


    @Override
    @SuppressWarnings("null")
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String[] allowedOrigins = Objects.requireNonNull(
                chatProperties.getWebsocket().getAllowedOriginPatterns().toArray(new String[0]));
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(Objects.requireNonNull(authenticatedUserInterceptor)).addPathPatterns("/api/**");
        String wsPath = chatProperties.getWebsocket().getEndpoint();
        registry.addInterceptor(Objects.requireNonNull(requestLoggingInterceptor))
                .addPathPatterns("/**")
                .excludePathPatterns(wsPath, wsPath + "/**");
    }
}
