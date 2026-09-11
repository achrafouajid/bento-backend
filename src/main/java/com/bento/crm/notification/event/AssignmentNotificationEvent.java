package com.bento.crm.notification.event;

import com.bento.crm.notification.model.Notification;
import java.util.UUID;

/**
 * Published whenever a member is assigned to a task, lead (partner) or ticket.
 *
 * <p>Carries already-resolved tenant + recipient so the listener can persist the
 * notification without touching request-scoped state (SOLID: services depend on the
 * {@code ApplicationEventPublisher} abstraction, not on {@code NotificationService}).
 */
public record AssignmentNotificationEvent(
        UUID organizationId,
        UUID recipientUserId,
        UUID actorUserId,
        Notification.NotificationType type,
        String title,
        String message,
        String relatedEntityType,
        UUID relatedEntityId) {
}
