package com.curatix.museumloan.museum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/museums")
@RequiredArgsConstructor
public class MuseumController {

    private final MuseumService museumService;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(Map.of("museums", museumService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(museumService.get(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody MuseumCreateRequest req) {
        return ResponseEntity.ok(museumService.create(req));
    }
}