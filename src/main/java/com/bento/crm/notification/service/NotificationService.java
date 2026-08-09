package com.bento.crm.notification.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.notification.model.Notification;
import com.bento.crm.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private static UUID getCurrentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public Page<Notification> listForCurrentUser(Pageable pageable) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return notificationRepository.findByRecipient(orgId, getCurrentUserId(), pageable);
    }

    @Transactional
    public Notification create(Notification notification) {
        notification.setOrganizationId(TenantContext.getCurrentOrganizationId());
        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markRead(UUID id) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        Notification notification = notificationRepository.findByOrganizationIdAndId(orgId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllReadForCurrentUser() {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        UUID userId = getCurrentUserId();
        Page<Notification> page = notificationRepository.findByRecipient(orgId, userId, Pageable.unpaged());
        page.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(page);
    }
}
