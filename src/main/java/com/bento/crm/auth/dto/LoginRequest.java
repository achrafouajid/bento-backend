package com.bento.crm.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 254, message = "Email must be at most 254 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 200, message = "Password must be at most 200 characters")
    private String password;

    /**
     * Optional tie-breaker for the rare account that shares an email <em>and</em> a password with
     * an account in another organization. Supplying it narrows the lookup; it can never widen it,
     * so it is not a credential and carries no privilege of its own.
     */
    @JsonProperty("organization_id")
    private UUID organizationId;
}
