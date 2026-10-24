package com.curatix.museumloan.loan;

import jakarta.validation.constraints.NotNull;

public record LoanCreateRequest(
        @NotNull Long artifactId,
        String notes
) {}