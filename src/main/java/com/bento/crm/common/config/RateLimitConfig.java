package com.bento.crm.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket resolveAuthBucket(String ipAddress) {
        String key = "auth_rate_limit:" + ipAddress;
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return cache.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(limit)
                        .build()
        );
    }

    /**
     * Looser than the signup bucket because the invitee legitimately hits it several times --
     * a preview on page load, another after a reload, then the accept itself -- and an invitee
     * may share an office IP with the admin who invited them. Brute-forcing a 256-bit token is
     * infeasible regardless of the limit; this is depth, not the primary defence.
     */
    public Bucket resolveInvitationBucket(String ipAddress) {
        String key = "invitation_rate_limit:" + ipAddress;
        Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofHours(1)));
        return cache.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(limit)
                        .build()
        );
    }

    public Bucket resolveSignupBucket(String ipAddress) {
        String key = "signup_rate_limit:" + ipAddress;
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofHours(1)));
        return cache.computeIfAbsent(key, k ->
                Bucket.builder()
                        .addLimit(limit)
                        .build()
        );
    }
}
