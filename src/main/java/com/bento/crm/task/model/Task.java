package com.bento.crm.task.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseTenantEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "uuid")
    private UUID assignedTeamId;

    @Column(columnDefinition = "uuid")
    private UUID assignedToUserId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID assignedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedEntityType relatedEntityType;

    @Column(columnDefinition = "uuid")
    private UUID relatedEntityId;

    public enum TaskStatus {
        TODO, IN_PROGRESS, DONE, BLOCKED
    }

    public enum Priority {
        URGENT, MEDIUM, LOW
    }

    public enum RelatedEntityType {
        PARTNER, DEAL, PROPOSAL, PURCHASE_ORDER, INVOICE, TICKET, CAMPAIGN
    }
}
