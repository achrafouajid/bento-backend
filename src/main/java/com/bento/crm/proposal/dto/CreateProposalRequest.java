package com.bento.crm.proposal.dto;

import com.bento.crm.proposal.model.Proposal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProposalRequest {

    @NotNull
    private UUID partnerId;

    private UUID templateId;

    @NotBlank
    private String title;

    @NotNull
    private Proposal.ProposalStatus status;

    private String deliveryMethod;

    private BigDecimal opportunityValue;

    private Integer closingProbability;

    private LocalDate expectedClosingDate;

    private String[] competitors;

    private String confirmationMethod;

    private String confirmationAttachmentFileId;

    private String confirmationNote;

    private Instant confirmedAt;

    private Instant sentAt;
}
