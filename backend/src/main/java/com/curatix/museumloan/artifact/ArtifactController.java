package com.curatix.museumloan.artifact;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private final ArtifactService artifactService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long museumId) {
        if (museumId != null) {
            return ResponseEntity.ok(Map.of("artifacts", artifactService.listByMuseum(museumId)));
        }
        return ResponseEntity.ok(Map.of("artifacts", artifactService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(artifactService.get(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ArtifactCreateRequest req,
                                     @AuthenticationPrincipal Long userId) {
        try {
            return ResponseEntity.ok(artifactService.create(req, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}