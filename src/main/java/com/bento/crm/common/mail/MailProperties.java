package com.bento.crm.common.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mail")
@Data
public class MailProperties {

    /**
     * Envelope and header From address. Hostinger's relay only accepts a From that matches a
     * mailbox it hosts, so this has to be a real address on the authenticated domain.
     */
    private String from = "no-reply@crmbento.com";

    private String fromName = "Bento CRM";

    /** Optional: replies to a no-reply sender go here instead of bouncing. */
    private String replyTo = "";

    /**
     * When false, {@link EmailService} logs the message instead of dispatching it. Local
     * development therefore needs no SMTP server and cannot email a real person by accident.
     */
    private boolean enabled = true;
}
