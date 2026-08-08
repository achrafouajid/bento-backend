package com.bento.crm.auth.controller;

import com.bento.crm.auth.dto.LoginRequest;
import com.bento.crm.auth.dto.LoginResponse;
import com.bento.crm.auth.dto.RefreshTokenRequest;
import com.bento.crm.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password", description = "Authenticate and receive JWT tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Use refresh token to get new access token")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate refresh tokens")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null) {
            UUID userId = UUID.fromString(authentication.getName());
            authService.logout(userId);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Get authenticated user information")
    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
        if (authentication != null) {
            return ResponseEntity.ok("User: " + authentication.getName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
