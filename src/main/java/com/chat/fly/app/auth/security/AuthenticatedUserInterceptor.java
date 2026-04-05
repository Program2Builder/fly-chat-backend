package com.chat.fly.app.auth.security;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticatedUserInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USERNAME_ATTRIBUTE = "currentUsername";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            request.setAttribute(CURRENT_USERNAME_ATTRIBUTE, authentication.getName());
        }
        return true;
    }
}
