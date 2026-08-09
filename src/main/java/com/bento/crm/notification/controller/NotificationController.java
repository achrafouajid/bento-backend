package com.bento.crm.notification.controller;

import com.bento.crm.common.dto.PageResponse;
import com.bento.crm.notification.model.Notification;
import com.bento.crm.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Per-user notification inbox endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List my notifications", description = "List notifications for the current user")
    public ResponseEntity<PageResponse<Notification>> listMyNotifications(Pageable pageable) {
        Page<Notification> page = notificationService.listForCurrentUser(pageable);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a notification for a recipient")
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification created = notificationService.create(notification);
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
