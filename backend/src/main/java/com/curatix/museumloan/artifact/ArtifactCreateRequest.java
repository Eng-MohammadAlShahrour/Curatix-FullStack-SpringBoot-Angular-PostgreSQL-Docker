package com.curatix.museumloan.artifact;

import jakarta.validation.constraints.NotBlank;

public record ArtifactCreateRequest(
        @NotBlank String name,
        String era,
        String description
) {}