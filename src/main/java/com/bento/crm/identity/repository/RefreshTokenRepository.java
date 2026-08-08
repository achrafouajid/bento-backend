package com.bento.crm.identity.repository;

import com.bento.crm.identity.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.expiresAt > :now AND rt.revokedAt IS NULL")
    Optional<RefreshToken> findValidToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.id = :id")
    void revokeToken(@Param("id") UUID id, @Param("now") Instant now);
}
