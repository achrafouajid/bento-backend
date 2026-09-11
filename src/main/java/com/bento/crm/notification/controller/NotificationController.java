package com.bento.crm.notification.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.notification.dto.CreateNotificationRequest;
import com.bento.crm.notification.model.Notification;
import com.bento.crm.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Per-user notification inbox endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications", description = "List notifications for the current user")
    public ResponseEntity<PageResponse<Notification>> listMyNotifications(Pageable pageable) {
        Page<Notification> page = notificationService.listForCurrentUser(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    /**
     * Addressing a notification to somebody else is a broadcast primitive, so it is restricted to
     * roles that already administer the organization. Before this, any authenticated user could
     * push arbitrary titles and bodies into any colleague's inbox.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN_ACCESS') or hasAuthority('USERS_WRITE')")
    @Operation(summary = "Create notification", description = "Create a notification for a recipient in this organization")
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        Notification created = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification read", description = "Mark a single notification as read")
    public ResponseEntity<Notification> markRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications read", description = "Mark all of the current user's notifications as read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllReadForCurrentUser();
        return ResponseEntity.noContent().build();
    }
}
