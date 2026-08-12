package com.bento.crm.automation.dto;

import com.bento.crm.automation.model.AutomationRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRuleResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private Boolean isActive;
    private AutomationRule.Trigger trigger;
    private Map<String, Object> conditionGroups;
    private Map<String, Object> actions;
    private Integer priority;
    private Boolean stopOnMatch;
    private Integer version;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AutomationRuleResponse fromEntity(AutomationRule automationRule) {
        return AutomationRuleResponse.builder()
                .id(automationRule.getId())
                .organizationId(automationRule.getOrganizationId())
                .name(automationRule.getName())
                .description(automationRule.getDescription())
                .isActive(automationRule.getIsActive())
                .trigger(automationRule.getTrigger())
                .conditionGroups(automationRule.getConditionGroups())
                .actions(automationRule.getActions())
                .priority(automationRule.getPriority())
                .stopOnMatch(automationRule.getStopOnMatch())
                .version(automationRule.getVersion())
                .createdBy(automationRule.getCreatedBy())
                .updatedBy(automationRule.getUpdatedBy())
                .createdAt(automationRule.getCreatedAt() != null ?
                    LocalDateTime.from(automationRule.getCreatedAt()) : null)
                .updatedAt(automationRule.getUpdatedAt() != null ?
                    LocalDateTime.from(automationRule.getUpdatedAt()) : null)
                .build();
    }
}
