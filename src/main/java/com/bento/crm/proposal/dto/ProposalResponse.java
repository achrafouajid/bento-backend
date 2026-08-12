package com.bento.crm.proposal.dto;

import com.bento.crm.proposal.model.Proposal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalResponse {

    private UUID id;
    private UUID organizationId;
    private UUID partnerId;
    private UUID templateId;
    private String title;
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
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProposalResponse fromEntity(Proposal proposal) {
        return ProposalResponse.builder()
                .id(proposal.getId())
                .organizationId(proposal.getOrganizationId())
                .partnerId(proposal.getPartnerId())
                .templateId(proposal.getTemplateId())
                .title(proposal.getTitle())
                .status(proposal.getStatus())
                .deliveryMethod(proposal.getDeliveryMethod())
                .opportunityValue(proposal.getOpportunityValue())
                .closingProbability(proposal.getClosingProbability())
                .expectedClosingDate(proposal.getExpectedClosingDate())
                .competitors(proposal.getCompetitors())
                .confirmationMethod(proposal.getConfirmationMethod())
                .confirmationAttachmentFileId(proposal.getConfirmationAttachmentFileId())
                .confirmationNote(proposal.getConfirmationNote())
                .confirmedAt(proposal.getConfirmedAt())
                .sentAt(proposal.getSentAt())
                .createdBy(proposal.getCreatedBy())
                .updatedBy(proposal.getUpdatedBy())
                .createdAt(proposal.getCreatedAt() != null ?
                    LocalDateTime.from(proposal.getCreatedAt()) : null)
                .updatedAt(proposal.getUpdatedAt() != null ?
                    LocalDateTime.from(proposal.getUpdatedAt()) : null)
                .build();
    }
}
