package com.bento.crm.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum Permission {
    // Partner permissions
    PARTNERS_READ("PARTNERS_READ"),
    PARTNERS_CREATE("PARTNERS_CREATE"),
    PARTNERS_WRITE("PARTNERS_WRITE"),
    PARTNERS_DELETE("PARTNERS_DELETE"),

    // Deal permissions
    DEALS_READ("DEALS_READ"),
    DEALS_CREATE("DEALS_CREATE"),
    DEALS_WRITE("DEALS_WRITE"),
    DEALS_DELETE("DEALS_DELETE"),

    // Proposal permissions
    PROPOSALS_READ("PROPOSALS_READ"),
    PROPOSALS_CREATE("PROPOSALS_CREATE"),
    PROPOSALS_WRITE("PROPOSALS_WRITE"),
    PROPOSALS_DELETE("PROPOSALS_DELETE"),

    // Purchase Order permissions
    PURCHASE_ORDERS_READ("PURCHASE_ORDERS_READ"),
    PURCHASE_ORDERS_CREATE("PURCHASE_ORDERS_CREATE"),
    PURCHASE_ORDERS_WRITE("PURCHASE_ORDERS_WRITE"),
    PURCHASE_ORDERS_DELETE("PURCHASE_ORDERS_DELETE"),

    // Invoice permissions
    INVOICES_READ("INVOICES_READ"),
    INVOICES_CREATE("INVOICES_CREATE"),
    INVOICES_WRITE("INVOICES_WRITE"),
    INVOICES_DELETE("INVOICES_DELETE"),

    // Ticket permissions
    TICKETS_READ("TICKETS_READ"),
    TICKETS_CREATE("TICKETS_CREATE"),
    TICKETS_WRITE("TICKETS_WRITE"),
    TICKETS_DELETE("TICKETS_DELETE"),

    // Task permissions
    TASKS_READ("TASKS_READ"),
    TASKS_CREATE("TASKS_CREATE"),
    TASKS_WRITE("TASKS_WRITE"),
    TASKS_DELETE("TASKS_DELETE"),

    // Campaign permissions
    CAMPAIGNS_READ("CAMPAIGNS_READ"),
    CAMPAIGNS_CREATE("CAMPAIGNS_CREATE"),
    CAMPAIGNS_WRITE("CAMPAIGNS_WRITE"),
    CAMPAIGNS_DELETE("CAMPAIGNS_DELETE"),

    // Automation permissions
    AUTOMATION_READ("AUTOMATION_READ"),
    AUTOMATION_WRITE("AUTOMATION_WRITE"),

    // User & Team management
    USERS_READ("USERS_READ"),
    USERS_WRITE("USERS_WRITE"),
    TEAMS_READ("TEAMS_READ"),
    TEAMS_WRITE("TEAMS_WRITE"),

    // Analytics
    ANALYTICS_READ("ANALYTICS_READ"),

    // Admin
    ADMIN_ACCESS("ADMIN_ACCESS");

    private final String authority;

    public static Set<Permission> forRole(UserRole role) {
        return switch (role) {
            case ADMIN -> Set.of(
                    PARTNERS_READ, PARTNERS_CREATE, PARTNERS_WRITE, PARTNERS_DELETE,
                    DEALS_READ, DEALS_CREATE, DEALS_WRITE, DEALS_DELETE,
                    PROPOSALS_READ, PROPOSALS_CREATE, PROPOSALS_WRITE, PROPOSALS_DELETE,
                    PURCHASE_ORDERS_READ, PURCHASE_ORDERS_CREATE, PURCHASE_ORDERS_WRITE, PURCHASE_ORDERS_DELETE,
                    INVOICES_READ, INVOICES_CREATE, INVOICES_WRITE, INVOICES_DELETE,
                    TICKETS_READ, TICKETS_CREATE, TICKETS_WRITE, TICKETS_DELETE,
                    TASKS_READ, TASKS_CREATE, TASKS_WRITE, TASKS_DELETE,
                    CAMPAIGNS_READ, CAMPAIGNS_CREATE, CAMPAIGNS_WRITE, CAMPAIGNS_DELETE,
                    AUTOMATION_READ, AUTOMATION_WRITE,
                    USERS_READ, USERS_WRITE,
                    TEAMS_READ, TEAMS_WRITE,
                    ANALYTICS_READ,
                    ADMIN_ACCESS
            );
            case MANAGER -> Set.of(
                    PARTNERS_READ, PARTNERS_CREATE, PARTNERS_WRITE,
                    DEALS_READ, DEALS_CREATE, DEALS_WRITE,
                    PROPOSALS_READ, PROPOSALS_CREATE, PROPOSALS_WRITE,
                    PURCHASE_ORDERS_READ, PURCHASE_ORDERS_CREATE, PURCHASE_ORDERS_WRITE,
                    INVOICES_READ, INVOICES_CREATE, INVOICES_WRITE,
                    TICKETS_READ, TICKETS_WRITE,
                    TASKS_READ, TASKS_CREATE, TASKS_WRITE,
                    CAMPAIGNS_READ, CAMPAIGNS_CREATE, CAMPAIGNS_WRITE,
                    AUTOMATION_READ,
                    USERS_READ, TEAMS_READ,
                    ANALYTICS_READ
            );
            case SALESPERSON -> Set.of(
                    PARTNERS_READ, PARTNERS_CREATE, PARTNERS_WRITE,
                    DEALS_READ, DEALS_CREATE, DEALS_WRITE,
                    PROPOSALS_READ, PROPOSALS_CREATE, PROPOSALS_WRITE,
                    TASKS_READ, TASKS_CREATE, TASKS_WRITE,
                    ANALYTICS_READ
            );
            case SUPPORT -> Set.of(
                    PARTNERS_READ, PARTNERS_WRITE,
                    TICKETS_READ, TICKETS_CREATE, TICKETS_WRITE,
                    TASKS_READ, TASKS_CREATE, TASKS_WRITE,
                    ANALYTICS_READ
            );
            case VIEWER -> Set.of(
                    PARTNERS_READ, DEALS_READ, PROPOSALS_READ,
                    TICKETS_READ, TASKS_READ, ANALYTICS_READ
            );
        };
    }
}
