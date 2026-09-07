package com.bento.crm.proposal.dto;

import com.bento.crm.proposal.model.ProposalTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating and updating a proposal template.
 *
 * <p>Replaces binding the {@link ProposalTemplate} entity directly. With the entity bound, a
 * client could send an {@code id}, which turned {@code save()} into a merge over whatever row
 * carried that id — including one in another organization, whose contents were then overwritten
 * and whose {@code organization_id} was rewritten to the caller's.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalTemplateRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String subject;

    private String body;

    private ProposalTemplate.Channel channel;

    private Map<String, Object> variables;

    private List<Map<String, Object>> lines;

    @JsonProperty("image_file_id")
    @Size(max = 255)
    private String imageFileId;

    @JsonProperty("approval_status")
    @Size(max = 255)
    private String approvalStatus;

    public void applyTo(ProposalTemplate template) {
        template.setName(name);
        template.setSubject(subject);
        template.setBody(body);
        template.setChannel(channel);
        template.setVariables(variables);
        template.setLines(lines);
        template.setImageFileId(imageFileId);
        template.setApprovalStatus(approvalStatus);
    }
}
