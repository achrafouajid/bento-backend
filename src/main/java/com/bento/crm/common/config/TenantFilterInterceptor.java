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
                    // Invalid token, will be handled by Spring Security
                }
            }

            // Set default organization for development (no token provided)
            if (organizationId == null) {
                organizationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            }

            if (organizationId != null) {
                TenantContext.setCurrentOrganizationId(organizationId);
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("organizationFilter")
                        .setParameter("organizationId", organizationId);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
