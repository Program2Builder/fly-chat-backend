package com.chat.fly.app.chat.dto;

import java.util.List;

public record KeyRegistrationRequest(
    int registrationId,
    String identityPublicKey,
    SignedPreKeyRequest signedPreKey,
    List<OneTimePreKeyRequest> oneTimePreKeys
) {
    public record SignedPreKeyRequest(
        int keyId,
        String publicKey,
        String signature
    ) {}

    public record OneTimePreKeyRequest(
        int keyId,
        String publicKey
    ) {}
}
