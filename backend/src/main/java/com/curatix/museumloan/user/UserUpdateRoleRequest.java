package com.curatix.museumloan.user;

import jakarta.validation.constraints.NotNull;

public record UserUpdateRoleRequest(
        @NotNull UserRole role,
        Long museumId
) {}