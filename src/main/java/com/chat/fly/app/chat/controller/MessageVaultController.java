package com.chat.fly.app.chat.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chat.fly.app.chat.dto.VaultBatchRequest;
import com.chat.fly.app.chat.dto.VaultEntryResponse;
import com.chat.fly.app.chat.dto.VaultPageResponse;
import com.chat.fly.app.chat.dto.VaultStoreRequest;
import com.chat.fly.app.chat.service.MessageVaultService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST API for the client-side encrypted Message Vault.
 *
 * Security model: the server is zero-knowledge — it stores AES-GCM ciphertexts
 * derived from passwords that never leave the client. All endpoints are
 * authenticated via JWT; users can only access their OWN vault entries.
 */
@RestController
@RequestMapping("/api/chat/vault")
@RequiredArgsConstructor
public class MessageVaultController {

    private final MessageVaultService vaultService;

    // ── POST /api/chat/vault ─────────────────────────────────────────────────
    // Upsert a single vault entry for the authenticated user.

    @PostMapping
    public ResponseEntity<VaultEntryResponse> storeEntry(
            Principal principal,
            @Valid @RequestBody VaultStoreRequest request) {

        String userId = String.valueOf(vaultService.resolveUserId(principal.getName()));
        VaultEntryResponse response = vaultService.storeEntry(
                userId, request.messageId(), request.ciphertext(),
                request.senderId(), request.receiverId());
        return ResponseEntity.ok(response);
    }

    // ── POST /api/chat/vault/batch ───────────────────────────────────────────
    // Batch upsert — max 100 entries.

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Integer>> storeEntries(
            Principal principal,
            @Valid @RequestBody VaultBatchRequest request) {

        if (request.entries() == null || request.entries().isEmpty()) {
            return ResponseEntity.ok(Map.of("saved", 0));
        }
        if (request.entries().size() > 100) {
            throw new IllegalArgumentException("Batch size must not exceed 100 entries");
        }

        String userId = String.valueOf(vaultService.resolveUserId(principal.getName()));
        int saved = vaultService.storeEntries(userId, request.entries());
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    // ── GET /api/chat/vault?page=0&size=100 ──────────────────────────────────
    // Retrieve paginated vault entries for the authenticated user.

    @GetMapping
    public ResponseEntity<VaultPageResponse> getEntries(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        // Clamp page size to prevent abuse
        int clampedSize = Math.min(size, 500);
        String userId = String.valueOf(vaultService.resolveUserId(principal.getName()));
        VaultPageResponse response = vaultService.getEntries(
                userId, principal.getName(), PageRequest.of(page, clampedSize));
        return ResponseEntity.ok(response);
    }

    // ── GET /api/chat/vault/direct/{targetUsername} ─────────────────────────
    // Retrieve vault entries for a specific direct conversation.

    @GetMapping("/direct/{targetUsername}")
    public ResponseEntity<VaultPageResponse> getDirectEntries(
            Principal principal,
            @PathVariable String targetUsername,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        String userId = String.valueOf(vaultService.resolveUserId(principal.getName()));
        VaultPageResponse response = vaultService.getDirectEntries(
                userId, principal.getName(), targetUsername, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    // ── DELETE /api/chat/vault/{messageId} ───────────────────────────────────
    // Idempotent delete — always 204 even if entry didn't exist.

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteEntry(
            Principal principal,
            @PathVariable String messageId) {

        String userId = String.valueOf(vaultService.resolveUserId(principal.getName()));
        vaultService.deleteEntry(userId, messageId);
        return ResponseEntity.noContent().build();
    }

    // ── Error handling ───────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
