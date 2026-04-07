package com.chat.fly.app.chat.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VaultBatchRequest(

    @NotNull(message = "entries must not be null")
    @Size(max = 100, message = "Batch size must not exceed 100 entries")
    @Valid
    List<VaultStoreRequest> entries
) {}
