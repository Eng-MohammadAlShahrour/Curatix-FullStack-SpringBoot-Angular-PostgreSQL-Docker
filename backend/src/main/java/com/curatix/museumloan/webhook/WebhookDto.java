package com.curatix.museumloan.webhook;

import java.time.Instant;

public record WebhookDto(
        Long id,
        String name,
        String url,
        String eventTypes,
        Boolean active,
        Instant createdAt
) {
    public static WebhookDto from(Webhook w) {
        return new WebhookDto(w.getId(), w.getName(), w.getUrl(),
                w.getEventTypes(), w.getActive(), w.getCreatedAt());
    }
}