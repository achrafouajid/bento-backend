package com.bento.crm.auth.service;

import com.bento.crm.common.config.JwtProperties;
import com.bento.crm.common.model.Permission;
import com.bento.crm.common.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    /**
     * Claim that separates the two token kinds. Both are signed with the same key, so without
     * it a refresh token — which lives for 30 days — is indistinguishable from a 15-minute
     * access token and would authenticate any request that only requires {@code authenticated()}.
     */
    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID userId, UUID organizationId, UserRole role) {
        Set<Permission> permissions = Permission.forRole(role);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(CLAIM_TOKEN_TYPE, TYPE_ACCESS)
                .claim("org", organizationId.toString())
                .claim("role", role.name())
                .claim("authorities", permissions.stream()
                        .map(Permission::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiry()))
                .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, UUID organizationId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
                .claim("org", organizationId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiry()))
                .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = parseAccessToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public UUID extractOrganizationId(String token) {
        Claims claims = parseAccessToken(token);
        String orgId = claims.get("org", String.class);
        return UUID.fromString(orgId);
    }

    /**
     * Parses and validates an access token, rejecting anything that is not one.
     *
     * @throws io.jsonwebtoken.JwtException if the signature, expiry or token type is wrong
     */
    public Claims parseAccessToken(String token) {
        Claims claims = parseAllClaims(token);
        String type = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!TYPE_ACCESS.equals(type)) {
            throw new io.jsonwebtoken.JwtException("Not an access token");
        }
        return claims;
    }

    /** Same as {@link #parseAccessToken} but returns null instead of throwing. */
    public Claims tryParseAccessToken(String token) {
        try {
            return parseAccessToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtProperties.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
