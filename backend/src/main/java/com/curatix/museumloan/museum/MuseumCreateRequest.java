package com.curatix.museumloan.museum;

import jakarta.validation.constraints.NotBlank;

public record MuseumCreateRequest(
        @NotBlank String name,
        @NotBlank String country,
        String city
) {}