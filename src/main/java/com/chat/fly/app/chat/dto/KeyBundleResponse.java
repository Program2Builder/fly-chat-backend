package com.chat.fly.app.chat.dto;

public record KeyBundleResponse(
    String username,
    int registrationId,
    String identityPublicKey,
    PreKeyResponse signedPreKey,
    PreKeyResponse oneTimePreKey // Nullable if exhausted
) {
    public record PreKeyResponse(
        int keyId,
        String publicKey,
        String signature // Nullable for one-time prekeys
    ) {}
}
