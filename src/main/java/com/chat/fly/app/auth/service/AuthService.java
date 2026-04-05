package com.chat.fly.app.auth.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.chat.fly.app.auth.dto.AuthResponse;
import com.chat.fly.app.auth.dto.LoginRequest;
import com.chat.fly.app.auth.dto.RegisterRequest;
import com.chat.fly.app.auth.model.AppUser;
import com.chat.fly.app.auth.repository.AppUserRepository;
import com.chat.fly.app.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final ChatService chatService;

    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);
        chatService.ensureDefaultGroupsForUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        return buildResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password()));

        AppUser user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        return buildResponse(user, token);
    }

    public AuthResponse.UserProfileResponse getProfile(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return chatService.toUserProfileResponse(user);
    }

    private AuthResponse buildResponse(AppUser user, String token) {
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.extractExpiration(token),
                chatService.toUserProfileResponse(user));
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("A required field is blank");
        }
        return value.trim().toLowerCase();
    }
}
