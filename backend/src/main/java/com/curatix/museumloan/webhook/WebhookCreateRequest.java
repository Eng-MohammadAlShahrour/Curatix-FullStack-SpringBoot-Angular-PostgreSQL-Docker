package com.curatix.museumloan.webhook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WebhookCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "^https?://.+", message = "Must be http:// or https:// URL")
        @Size(max = 500) String url,
        @NotBlank String eventTypes,
        String secret,
        @NotNull Boolean active
) {}