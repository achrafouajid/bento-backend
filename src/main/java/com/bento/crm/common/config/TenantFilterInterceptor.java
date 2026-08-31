package com.bento.crm.common.config;

import com.bento.crm.common.context.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantFilterInterceptor extends OncePerRequestFilter {

    private final EntityManager entityManager;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            UUID organizationId = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtProperties.getSecretKey())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    String orgId = claims.get("org", String.class);
                    if (orgId != null) {
                        organizationId = UUID.fromString(orgId);
                    }
                } catch (Exception e) {
                    // Invalid token: leave organizationId null so the request is rejected below,
                    // unless the endpoint is public (auth/signup/health/docs), which carry no org claim.
                }
            }

            if (organizationId == null) {
                if (isPublicEndpoint(request)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Missing or invalid organization context\"}");
                return;
            }

            TenantContext.setCurrentOrganizationId(organizationId);
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("organizationFilter")
                    .setParameter("organizationId", organizationId);

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.endsWith("/auth/login")
                || path.endsWith("/auth/refresh")
                || path.endsWith("/organizations")
                // Invitation acceptance carries no JWT and therefore no org claim; the
                // invitation token resolves the tenant instead.
                || path.contains("/public/invitations")
                || path.contains("/actuator/health")
                || path.contains("/swagger-ui")
                || path.contains("/openapi")
                // The WhatsApp webhook carries no JWT and therefore no org claim; it
                // resolves its own tenant from metadata.phone_number_id instead.
                || path.contains("/webhooks/whatsapp");
    }
}
