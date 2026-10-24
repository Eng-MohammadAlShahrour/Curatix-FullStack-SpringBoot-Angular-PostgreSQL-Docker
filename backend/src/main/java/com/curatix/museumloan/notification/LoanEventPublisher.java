package com.curatix.museumloan.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(LoanEvent event) {
        publisher.publishEvent(event);
    }
}