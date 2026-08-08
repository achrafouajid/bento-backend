package com.bento.crm.common.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Getter
public class JwtProperties {

    private static final int MIN_SECRET_LENGTH = 32;

    private final String secret;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtProperties(
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_ACCESS_TOKEN_EXPIRY:900000}") long accessTokenExpiry,
            @Value("${JWT_REFRESH_TOKEN_EXPIRY:2592000000}") long refreshTokenExpiry) {
        if (secret == null || secret.getBytes().length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to a value of at least " + MIN_SECRET_LENGTH + " bytes");
        }
        this.secret = secret;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
