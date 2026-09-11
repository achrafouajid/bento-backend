package com.bento.crm.automation.dto;

import com.bento.crm.automation.model.AutomationRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAutomationRuleRequest {

    @NotBlank
    private String name;

    private String description;

    private Boolean isActive;

    @NotNull
    private AutomationRule.Trigger trigger;

    private Map<String, Object> conditionGroups;

    private Map<String, Object> actions;

    private Integer priority;

    private Boolean stopOnMatch;

    private Integer version;
}
