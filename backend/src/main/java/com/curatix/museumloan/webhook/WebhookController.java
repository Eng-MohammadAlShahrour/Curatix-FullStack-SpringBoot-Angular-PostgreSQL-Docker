package com.curatix.museumloan.webhook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/webhooks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WebhookController {

    private final WebhookService webhookService;
    private final WebhookDeliveryRepository deliveryRepository;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(Map.of("webhooks", webhookService.listAll()));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody WebhookCreateRequest req) {
        return ResponseEntity.ok(webhookService.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        webhookService.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/{id}/deliveries")
    public ResponseEntity<?> deliveries(@PathVariable Long id,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Page<WebhookDelivery> result = deliveryRepository.findByWebhookIdOrderByAttemptedAtDesc(
                id, PageRequest.of(Math.max(page, 0), Math.min(size, 100)));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("content", result.getContent());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<?> allDeliveries(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Page<WebhookDelivery> result = deliveryRepository.findAllByOrderByAttemptedAtDesc(
                PageRequest.of(Math.max(page, 0), Math.min(size, 100)));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("content", result.getContent());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        return ResponseEntity.ok(resp);
    }
}