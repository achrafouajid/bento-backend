package com.bento.crm.ticket.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseTenantEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(columnDefinition = "uuid")
    private UUID assignedToUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDate deadline;

    @Column(columnDefinition = "text")
    private String resolution;

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
