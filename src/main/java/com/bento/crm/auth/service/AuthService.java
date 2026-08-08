package com.bento.crm.auth.service;

import com.bento.crm.auth.dto.LoginRequest;
import com.bento.crm.auth.dto.LoginResponse;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.common.model.UserRole;
import com.bento.crm.identity.dto.UserResponseDto;
import com.bento.crm.identity.mapper.UserMapper;
import com.bento.crm.identity.model.AppUser;
import com.bento.crm.identity.model.RefreshToken;
import com.bento.crm.identity.repository.AppUserRepository;
import com.bento.crm.identity.repository.RefreshTokenRepository;
import com.bento.crm.organization.model.Organization;
import com.bento.crm.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AppUserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public LoginResponse login(String email, String password) {
        // Find user by email across all organizations (federation lookup)
        var users = userRepository.findAll();
        AppUser user = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new IllegalStateException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setLastActiveAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getOrganizationId(), user.getRole());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId(), user.getOrganizationId());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshTokenValue))
                .expiresAt(Instant.now().plusSeconds(2592000))
                .build();
        refreshTokenRepository.save(refreshToken);

        UserResponseDto userDto = userMapper.toResponseDto(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userDto)
                .build();
    }

    @Transactional
    public LoginResponse refresh(String refreshTokenValue) {
        String tokenHash = hashToken(refreshTokenValue);
        RefreshToken refreshToken = refreshTokenRepository.findValidToken(tokenHash, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

        AppUser user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeToken(refreshToken.getId(), Instant.now());

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getOrganizationId(), user.getRole());
        String newRefreshTokenValue = jwtService.generateRefreshToken(user.getId(), user.getOrganizationId());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(newRefreshTokenValue))
                .expiresAt(Instant.now().plusSeconds(2592000))
                .replacedByTokenId(refreshToken.getId())
                .build();
        refreshTokenRepository.save(newRefreshToken);

        UserResponseDto userDto = userMapper.toResponseDto(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userDto)
                .build();
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, Instant.now());
        log.info("User {} logged out", userId);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
