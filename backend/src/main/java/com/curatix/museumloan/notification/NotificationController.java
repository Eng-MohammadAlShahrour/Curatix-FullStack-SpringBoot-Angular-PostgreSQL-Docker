package com.curatix.museumloan.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationLogRepository repo;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);

        Page<NotificationLog> result;
        if (recipient != null)     result = repo.findByRecipientOrderBySentAtDesc(recipient, pageable);
        else if (status != null)   result = repo.findByStatusOrderBySentAtDesc(status, pageable);
        else if (eventType != null) result = repo.findByEventTypeOrderBySentAtDesc(eventType, pageable);
        else                       result = repo.findAllByOrderBySentAtDesc(pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("hasNext", result.hasNext());

        return ResponseEntity.ok(response);
    }
}