package com.chat.fly.app.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.chat.fly.app.auth.security.AuthenticatedUserInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTR = RequestLoggingInterceptor.class.getName() + ".startTime";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           @Nullable ModelAndView modelAndView) {
        // intentionally empty — log in afterCompletion so status is final
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long durationMs = startTime != null ? System.currentTimeMillis() - startTime : -1;

        String user = (String) request.getAttribute(AuthenticatedUserInterceptor.CURRENT_USERNAME_ATTRIBUTE);
        String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";

        log.info("{} {}{} | status={} | user={} | {}ms",
                request.getMethod(),
                request.getRequestURI(),
                query,
                response.getStatus(),
                user != null ? user : "anonymous",
                durationMs);
    }
}
