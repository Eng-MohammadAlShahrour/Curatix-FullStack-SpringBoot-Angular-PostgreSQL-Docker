package com.curatix.museumloan.auth.dto;

import com.curatix.museumloan.user.UserRole;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank String fullName,
        @NotNull UserRole role,
        Long museumId
) {}