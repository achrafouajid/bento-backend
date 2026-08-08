package com.bento.crm.common.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Getter
public class JwtProperties {

    private final String secret;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtProperties(
            @Value("${JWT_SECRET:your-secret-key-change-this-in-production}") String secret,
            @Value("${JWT_ACCESS_TOKEN_EXPIRY:900000}") long accessTokenExpiry,
            @Value("${JWT_REFRESH_TOKEN_EXPIRY:2592000000}") long refreshTokenExpiry) {
        this.secret = secret;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
