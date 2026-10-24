package com.curatix.museumloan.artifact;

import java.time.Instant;

public record ArtifactDto(
        Long id, String name, String era, String description,
        String status, Long museumId, String museumName, Instant createdAt
) {
    public static ArtifactDto from(Artifact a) {
        return new ArtifactDto(
                a.getId(), a.getName(), a.getEra(), a.getDescription(),
                a.getStatus(), a.getMuseum().getId(), a.getMuseum().getName(),
                a.getCreatedAt()
        );
    }
}