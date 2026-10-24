package com.curatix.museumloan.webhook;

import com.curatix.museumloan.notification.LoanEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;

    // Instantiate locally to avoid dependency on Spring bean
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public List<WebhookDto> listAll() {
        return webhookRepository.findAll().stream()
                .map(WebhookDto::from)
                .toList();
    }

    @Transactional
    public WebhookDto create(WebhookCreateRequest req) {
        Webhook w = Webhook.builder()
                .name(req.name())
                .url(req.url())
                .eventTypes(req.eventTypes())
                .secret(req.secret())
                .active(req.active())
                .build();
        webhookRepository.save(w);
        return WebhookDto.from(w);
    }

    @Transactional
    public void delete(Long id) {
        webhookRepository.deleteById(id);
    }

    @Async
    public void dispatch(LoanEvent event) {
        List<Webhook> webhooks = webhookRepository.findByActiveTrue();
        for (Webhook webhook : webhooks) {
            if (!matchesEvent(webhook.getEventTypes(), event.type().name())) {
                continue;
            }
            try {
                deliver(webhook, event);
            } catch (Exception ex) {
                log.warn("Webhook delivery failed for {}: {}", webhook.getName(), ex.getMessage());
            }
        }
    }

    private boolean matchesEvent(String eventTypes, String eventType) {
        if (eventTypes == null || eventTypes.isBlank()) return false;
        for (String e : eventTypes.split(",")) {
            if (e.trim().equalsIgnoreCase(eventType)) return true;
            if (e.trim().equals("*")) return true;
        }
        return false;
    }

    private void deliver(Webhook webhook, LoanEvent event) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event.type().name());
        payload.put("timestamp", Instant.now().toString());
        payload.put("loan", Map.of(
                "id", event.loanId(),
                "artifact", event.artifactName(),
                "fromMuseum", event.fromMuseumName(),
                "toMuseum", event.toMuseumName(),
                "recipientEmail", event.recipientEmail(),
                "recipientName", event.recipientName()
        ));

        String body = objectMapper.writeValueAsString(payload);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(webhook.getUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Curatix-Webhook/1.0")
                .header("X-Curatix-Event", event.type().name())
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (webhook.getSecret() != null && !webhook.getSecret().isBlank()) {
            String signature = hmacSha256(webhook.getSecret(), body);
            builder.header("X-Curatix-Signature", "sha256=" + signature);
        }

        HttpResponse<String> response = httpClient.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

        boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
        persistDelivery(webhook.getId(), event.type().name(), body,
                response.statusCode(), response.body(), success, null);

        log.info("Webhook {} → {} → HTTP {}",
                webhook.getName(), event.type().name(), response.statusCode());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void persistDelivery(Long webhookId, String eventType, String payload,
                         Integer status, String body, boolean success, String error) {
        WebhookDelivery d = WebhookDelivery.builder()
                .webhookId(webhookId)
                .eventType(eventType)
                .payload(payload)
                .responseStatus(status)
                .responseBody(body)
                .success(success)
                .errorMessage(error)
                .build();
        deliveryRepository.save(d);
    }

    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}