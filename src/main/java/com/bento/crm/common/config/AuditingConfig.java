package com.bento.crm.common.config;

import com.bento.crm.common.context.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principal
                    && !"anonymousUser".equals(principal)) {
                try {
                    return Optional.of(UUID.fromString(principal));
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        };
    }
}
