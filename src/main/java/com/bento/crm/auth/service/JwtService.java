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

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID userId, UUID organizationId, UserRole role) {
        Set<Permission> permissions = Permission.forRole(role);

        return Jwts.builder()
                .setSubject(userId.toString())
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
                .claim("org", organizationId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiry()))
                .signWith(jwtProperties.getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public UUID extractOrganizationId(String token) {
        Claims claims = extractAllClaims(token);
        String orgId = claims.get("org", String.class);
        return UUID.fromString(orgId);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtProperties.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
