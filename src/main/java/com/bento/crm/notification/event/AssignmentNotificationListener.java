package com.bento.crm.notification.event;

import com.bento.crm.identity.repository.AppUserRepository;
import com.bento.crm.notification.model.Notification;
import com.bento.crm.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Single responsibility: turn assignment events into inbox rows.
 *
 * <p>Runs synchronously in the publisher's transaction (not
 * {@code @TransactionalEventListener(AFTER_COMMIT)}): the notification row commits
 * atomically with the assignment itself, so a rolled-back assignment can never leave a
 * phantom notification behind — and the event can never be silently dropped when no
 * transaction synchronization is active.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentNotificationListener {

    private final NotificationService notificationService;
    private final AppUserRepository userRepository;

    @EventListener
    public void onAssignment(AssignmentNotificationEvent event) {
        if (event.recipientUserId() == null || event.organizationId() == null) {
            return;
        }
        // No noise when someone assigns work to themselves.
        if (event.recipientUserId().equals(event.actorUserId())) {
            return;
        }
        // Tenant guard: never notify a user that does not belong to the org.
        boolean member = userRepository
                .findByOrganizationIdAndId(event.organizationId(), event.recipientUserId())
                .isPresent();
        if (!member) {
            log.warn("Skipping assignment notification for unknown user {} in org {}",
                    event.recipientUserId(), event.organizationId());
            return;
        }

        Notification notification = Notification.builder()
                .recipientUserId(event.recipientUserId())
                .type(event.type())
                .title(event.title())
                .message(event.message())
                .relatedEntityType(event.relatedEntityType())
                .relatedEntityId(event.relatedEntityId())
                .isRead(false)
                .build();
        notificationService.createForOrganization(event.organizationId(), notification);
    }
}
