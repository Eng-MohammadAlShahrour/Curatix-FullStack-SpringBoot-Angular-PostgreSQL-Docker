package com.curatix.museumloan.metrics;

import com.curatix.museumloan.loan.LoanStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom business metrics for the loan lifecycle.
 * Exposed via Micrometer → Prometheus → Grafana.
 */
@Component
public class LoanMetrics {

    private final Map<LoanStatus, Counter> transitionCounters = new EnumMap<>(LoanStatus.class);
    private final Counter loansCreatedCounter;
    private final Counter rejectedCounter;
    private final Timer loanApprovalTimer;

    private final AtomicLong pendingCount = new AtomicLong();
    private final AtomicLong shippedCount = new AtomicLong();
    private final AtomicLong activeLoans = new AtomicLong();

    public LoanMetrics(MeterRegistry registry) {
        this.loansCreatedCounter = Counter.builder("curatix_loans_created_total")
                .description("Total loan requests created")
                .register(registry);

        this.rejectedCounter = Counter.builder("curatix_loans_rejected_total")
                .description("Total loans rejected")
                .register(registry);

        this.loanApprovalTimer = Timer.builder("curatix_loan_approval_duration_seconds")
                .description("Time from loan creation to approval")
                .publishPercentileHistogram(true)
                .register(registry);

        for (LoanStatus status : LoanStatus.values()) {
            transitionCounters.put(status, Counter.builder("curatix_loan_transitions_total")
                    .description("Loan state transitions")
                    .tag("status", status.name())
                    .register(registry));
        }

        Gauge.builder("curatix_loans_pending", pendingCount, AtomicLong::get)
                .description("Current number of pending loans")
                .register(registry);

        Gauge.builder("curatix_loans_shipped", shippedCount, AtomicLong::get)
                .description("Current number of shipped loans")
                .register(registry);

        Gauge.builder("curatix_loans_active", activeLoans, AtomicLong::get)
                .description("Active loans (approved or shipped)")
                .register(registry);
    }

    public void recordLoanCreated() {
        loansCreatedCounter.increment();
        transitionCounters.get(LoanStatus.PENDING).increment();
        pendingCount.incrementAndGet();
    }

    public void recordTransition(LoanStatus newStatus) {
        Counter c = transitionCounters.get(newStatus);
        if (c != null) c.increment();

        switch (newStatus) {
            case APPROVED -> {
                pendingCount.decrementAndGet();
                activeLoans.incrementAndGet();
            }
            case REJECTED -> {
                pendingCount.decrementAndGet();
                rejectedCounter.increment();
            }
            case SHIPPED -> {
                shippedCount.incrementAndGet();
            }
            case RETURNED -> {
                shippedCount.decrementAndGet();
                activeLoans.decrementAndGet();
            }
            default -> {}
        }
    }

    public Timer.Sample startApprovalTimer() {
        return Timer.start();
    }

    public void stopApprovalTimer(Timer.Sample sample) {
        sample.stop(loanApprovalTimer);
    }
}