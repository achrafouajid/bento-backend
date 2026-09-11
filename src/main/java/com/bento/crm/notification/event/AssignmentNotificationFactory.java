package com.bento.crm.notification.event;

import com.bento.crm.notification.model.Notification;
import java.util.UUID;

/**
 * Single place that knows how assignment notifications read (SRP).
 * Add a new {@code forXxx()} factory when a new assignable entity appears —
 * the listener itself stays closed for modification (OCP).
 */
public final class AssignmentNotificationFactory {

    private AssignmentNotificationFactory() {
    }

    public static AssignmentNotificationEvent forTask(
            UUID organizationId, UUID recipientUserId, UUID actorUserId,
            UUID taskId, String taskTitle) {
        return new AssignmentNotificationEvent(
                organizationId,
                recipientUserId,
                actorUserId,
                Notification.NotificationType.TASK,
                "You were assigned a task",
                "Task \"" + safe(taskTitle) + "\" was assigned to you.",
                "TASK",
                taskId);
    }

    public static AssignmentNotificationEvent forTicket(
            UUID organizationId, UUID recipientUserId, UUID actorUserId,
            UUID ticketId, String ticketTitle) {
        return new AssignmentNotificationEvent(
                organizationId,
                recipientUserId,
                actorUserId,
                Notification.NotificationType.TICKET,
                "You were assigned a ticket",
                "Ticket \"" + safe(ticketTitle) + "\" was assigned to you.",
                "TICKET",
                ticketId);
    }

    public static AssignmentNotificationEvent forLead(
            UUID organizationId, UUID recipientUserId, UUID actorUserId,
            UUID leadId, String leadName) {
        return new AssignmentNotificationEvent(
                organizationId,
                recipientUserId,
                actorUserId,
                Notification.NotificationType.LEAD,
                "You were assigned a lead",
                "Lead \"" + safe(leadName) + "\" was assigned to you.",
                "LEAD",
                leadId);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
