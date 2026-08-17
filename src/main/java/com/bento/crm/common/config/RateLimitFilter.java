package com.bento.crm.common.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// Disabled in integration tests (see IntegrationTestBase) -- the signup/auth buckets are scoped
// per-IP with production-appropriate limits (e.g. 5 signups/hour), which a test suite blows
// through in seconds since every MockMvc request comes from the same loopback address.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        Bucket bucket = selectBucket(path, request.getMethod(), clientIp);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Retry after " + waitForRefill + " seconds.");
        }
    }

    private Bucket selectBucket(String path, String method, String clientIp) {
        if (path.contains("/auth/login")) {
            return rateLimitConfig.resolveAuthBucket(clientIp);
        } else if (path.endsWith("/organizations") && "POST".equalsIgnoreCase(method)) {
            return rateLimitConfig.resolveSignupBucket(clientIp);
        } else {
            return rateLimitConfig.resolveBucket(clientIp);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/actuator") || path.contains("/swagger-ui") || path.contains("/openapi");
    }
}
