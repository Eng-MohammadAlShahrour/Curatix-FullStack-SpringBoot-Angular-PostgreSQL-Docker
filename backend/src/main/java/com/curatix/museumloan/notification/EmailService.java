package com.curatix.museumloan.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailTemplateBuilder templateBuilder;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${curatix.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:no-reply@curatix.test}")
    private String fromAddress;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendLoanEmail(LoanEvent event) {
        String subject = buildSubject(event);
        String body = templateBuilder.build(event);

        NotificationStatus status;
        String error = null;

        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (emailEnabled && mailSender != null) {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(event.recipientEmail());
                helper.setSubject(subject);
                helper.setText(body, true);
                mailSender.send(msg);
                status = NotificationStatus.SENT;
                log.info("📧 Email sent to {} for loan #{} ({})",
                        event.recipientEmail(), event.loanId(), event.type());
            } else {
                status = NotificationStatus.SKIPPED;
                log.info("📧 [DEV MODE] Would send '{}' to {} for loan #{}",
                        subject, event.recipientEmail(), event.loanId());
            }
        } catch (Exception ex) {
            status = NotificationStatus.FAILED;
            error = ex.getMessage();
            log.warn("Failed to send email to {}: {}", event.recipientEmail(), ex.getMessage());
        }

        try {
            notificationLogRepository.save(NotificationLog.builder()
                    .recipient(event.recipientEmail())
                    .subject(subject)
                    .body(body)
                    .eventType(event.type().name())
                    .status(status)
                    .errorMessage(error)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to persist notification log: {}", ex.getMessage());
        }
    }

    private String buildSubject(LoanEvent event) {
        return switch (event.type()) {
            case LOAN_CREATED   -> "Curatix — New loan request: " + event.artifactName();
            case LOAN_APPROVED  -> "Curatix — Loan approved: " + event.artifactName();
            case LOAN_REJECTED  -> "Curatix — Loan rejected: " + event.artifactName();
            case LOAN_SHIPPED   -> "Curatix — Artifact shipped: " + event.artifactName();
            case LOAN_RETURNED  -> "Curatix — Artifact returned: " + event.artifactName();
        };
    }
}