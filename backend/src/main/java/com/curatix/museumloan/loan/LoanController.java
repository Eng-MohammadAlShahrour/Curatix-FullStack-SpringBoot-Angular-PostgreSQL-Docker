package com.curatix.museumloan.loan;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) LoanStatus status) {
        if (status != null) {
            return ResponseEntity.ok(Map.of("loans", loanService.listByStatus(status)));
        }
        return ResponseEntity.ok(Map.of("loans", loanService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(loanService.get(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody LoanCreateRequest req,
                                     @AuthenticationPrincipal Long userId) {
        try {
            return ResponseEntity.ok(loanService.create(req, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        try {
            return ResponseEntity.ok(loanService.approve(id, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        try {
            return ResponseEntity.ok(loanService.reject(id, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<?> ship(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        try {
            return ResponseEntity.ok(loanService.ship(id, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<?> returnArtifact(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        try {
            return ResponseEntity.ok(loanService.returnArtifact(id, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}