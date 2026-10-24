package com.curatix.museumloan.user;

import java.time.Instant;

public record UserDto(
        Long id,
        String email,
        String fullName,
        UserRole role,
        Long museumId,
        String museumName,
        Instant createdAt
) {}