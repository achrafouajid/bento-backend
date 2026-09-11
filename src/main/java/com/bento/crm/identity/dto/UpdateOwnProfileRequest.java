package com.bento.crm.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fields a user may update on their own profile without USERS_WRITE.
 * Role and team assignment are deliberately excluded -- those still
 * go through UpdateUserRequest / PATCH /users/{id}, which requires
 * USERS_WRITE.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOwnProfileRequest {

    @JsonProperty("display_name")
    private String displayName;

    private String phone;

    @JsonProperty("job_title")
    private String jobTitle;

    @Pattern(regexp = "en|fr|ar", message = "language must be one of: en, fr, ar")
    private String language;
}
