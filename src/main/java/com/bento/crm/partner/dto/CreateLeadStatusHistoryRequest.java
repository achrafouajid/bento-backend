package com.bento.crm.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadStatusHistoryRequest {

    @NotBlank
    private String status;

    @JsonProperty("changed_by_user_id")
    private String changedByUserId;
}
