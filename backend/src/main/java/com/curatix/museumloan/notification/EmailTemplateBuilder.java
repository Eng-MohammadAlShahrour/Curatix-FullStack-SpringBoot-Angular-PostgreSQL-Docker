package com.curatix.museumloan.notification;

import org.springframework.stereotype.Component;

/**
 * Builds HTML email templates matching the Curatix brand
 * (navy + gold palette, serif typography).
 */
@Component
public class EmailTemplateBuilder {

    public String build(LoanEvent event) {
        String headline = switch (event.type()) {
            case LOAN_CREATED   -> "New Loan Request";
            case LOAN_APPROVED  -> "Loan Approved";
            case LOAN_REJECTED  -> "Loan Rejected";
            case LOAN_SHIPPED   -> "Artifact Shipped";
            case LOAN_RETURNED  -> "Artifact Returned";
        };

        String message = switch (event.type()) {
            case LOAN_CREATED   -> "A new loan request has been created for the artifact below.";
            case LOAN_APPROVED  -> "Good news — your loan request has been approved by the reviewer.";
            case LOAN_REJECTED  -> "Unfortunately, your loan request has been rejected.";
            case LOAN_SHIPPED   -> "The artifact is on its way. Please confirm safe receipt.";
            case LOAN_RETURNED  -> "The artifact has been returned to its home museum. The loan is now closed.";
        };

        return """
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <style>
                    body { background: #F8F7F4; font-family: -apple-system, sans-serif; margin: 0; padding: 40px 20px; }
                    .card { max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden;
                            box-shadow: 0 4px 16px rgba(10,37,64,0.06); }
                    .header { background: #0A2540; padding: 32px; text-align: center; color: #ffffff; }
                    .header h1 { font-family: Georgia, serif; font-size: 24px; margin: 0 0 4px 0; color: #ffffff; }
                    .header .brand { font-size: 11px; letter-spacing: 3px; text-transform: uppercase; color: rgba(255,255,255,0.6); }
                    .content { padding: 32px; color: #0A2540; }
                    .content h2 { font-family: Georgia, serif; font-size: 20px; margin: 0 0 12px 0; color: #0A2540; }
                    .content p { color: #52606D; line-height: 1.6; margin: 0 0 20px 0; }
                    .details { background: #F8F7F4; border-radius: 12px; padding: 20px; margin: 20px 0; }
                    .details .row { display: flex; justify-content: space-between; padding: 6px 0;
                                    border-bottom: 1px solid #EEEBE3; font-size: 14px; }
                    .details .row:last-child { border-bottom: none; }
                    .details .label { color: #7B8794; }
                    .details .value { color: #0A2540; font-weight: 500; }
                    .footer { padding: 24px 32px; text-align: center; font-size: 12px; color: #7B8794;
                              border-top: 1px solid #EEEBE3; }
                    .gold { color: #C9A961; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <div class="header">
                      <div class="brand">Curatix</div>
                      <h1>%s</h1>
                    </div>
                    <div class="content">
                      <p>Hello <strong>%s</strong>,</p>
                      <p>%s</p>
                      <div class="details">
                        <div class="row"><span class="label">Artifact</span><span class="value">%s</span></div>
                        <div class="row"><span class="label">From</span><span class="value">%s</span></div>
                        <div class="row"><span class="label">To</span><span class="value">%s</span></div>
                        <div class="row"><span class="label">Loan ID</span><span class="value">#%d</span></div>
                      </div>
                      <p style="font-size:13px;color:#7B8794;">You can manage this loan by signing in to your Curatix dashboard.</p>
                    </div>
                    <div class="footer">
                      Curatix — <span class="gold">Museum Loans</span><br>
                      © 2026 · Enterprise Edition
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                        headline,
                        event.recipientName(),
                        message,
                        event.artifactName(),
                        event.fromMuseumName(),
                        event.toMuseumName(),
                        event.loanId()
                );
    }
}