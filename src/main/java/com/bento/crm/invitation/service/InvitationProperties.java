package com.bento.crm.invitation.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.invitation")
@Data
public class InvitationProperties {

    /**
     * Frontend route that renders the acceptance form. The token is appended as {@code ?token=},
     * so this must address the SPA, not the API.
     */
    private String acceptUrl = "http://localhost:3000/invite/accept";

    private int expiryDays = 7;
}
