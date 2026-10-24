package com.curatix.museumloan.loan;

import java.time.Instant;

public record LoanDto(
        Long id, Long artifactId, String artifactName,
        Long fromMuseumId, String fromMuseumName,
        Long toMuseumId, String toMuseumName,
        Long requestedById, String requestedByName,
        String status, String notes,
        Instant createdAt, Instant updatedAt
) {
    public static LoanDto from(LoanRequest r) {
        return new LoanDto(
                r.getId(),
                r.getArtifact().getId(), r.getArtifact().getName(),
                r.getFromMuseum().getId(), r.getFromMuseum().getName(),
                r.getToMuseum().getId(), r.getToMuseum().getName(),
                r.getRequestedBy().getId(), r.getRequestedBy().getFullName(),
                r.getStatus().name(), r.getNotes(),
                r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}