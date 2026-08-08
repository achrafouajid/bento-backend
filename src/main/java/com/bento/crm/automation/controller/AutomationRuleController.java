package com.bento.crm.automation.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.automation.model.AutomationRule;
import com.bento.crm.automation.service.AutomationRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/automation-rules")
@Tag(name = "Automation Rules", description = "Automation rule management endpoints")
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;

    public AutomationRuleController(AutomationRuleService automationRuleService) {
        this.automationRuleService = automationRuleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AUTOMATION_RULES_CREATE')")
    @Operation(summary = "Create automation rule", description = "Create a new automation rule")
    public ResponseEntity<AutomationRule> createAutomationRule(@Valid @RequestBody AutomationRule rule) {
        AutomationRule created = automationRuleService.createAutomationRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTOMATION_RULES_READ')")
    @Operation(summary = "Get automation rule by ID", description = "Retrieve automation rule details")
    public ResponseEntity<AutomationRule> getAutomationRule(@PathVariable UUID id) {
        AutomationRule rule = automationRuleService.getAutomationRule(id);
        return ResponseEntity.ok(rule);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUTOMATION_RULES_READ')")
    @Operation(summary = "List automation rules", description = "List all automation rules in the organization")
    public ResponseEntity<PageResponse<AutomationRule>> listAutomationRules(Pageable pageable) {
        Page<AutomationRule> page = automationRuleService.listAutomationRules(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTOMATION_RULES_WRITE')")
    @Operation(summary = "Update automation rule", description = "Update automation rule information")
    public ResponseEntity<AutomationRule> updateAutomationRule(@PathVariable UUID id, @Valid @RequestBody AutomationRule updates) {
        AutomationRule rule = automationRuleService.updateAutomationRule(id, updates);
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTOMATION_RULES_DELETE')")
    @Operation(summary = "Delete automation rule", description = "Delete automation rule record")
    public ResponseEntity<Void> deleteAutomationRule(@PathVariable UUID id) {
        automationRuleService.deleteAutomationRule(id);
        return ResponseEntity.noContent().build();
    }
}
