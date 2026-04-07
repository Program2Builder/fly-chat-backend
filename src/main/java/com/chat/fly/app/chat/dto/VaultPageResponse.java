package com.chat.fly.app.chat.dto;

import java.util.List;

public record VaultPageResponse(
    List<VaultEntryResponse> entries,
    boolean hasMore,
    int page,
    int size
) {}
