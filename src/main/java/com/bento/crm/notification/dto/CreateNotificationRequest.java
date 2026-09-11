package com.bento.crm.notification.dto;

import com.bento.crm.notification.model.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request body for creating a notification.
 *
 * <p>Replaces binding the {@link Notification} entity directly. Accepting the entity let a client
 * supply {@code id}, which turned the save into a merge over an existing row — including one
 * belonging to another tenant — and also let it set {@code organizationId}, {@code createdAt} and
 * the audit columns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "recipient_user_id is required")
    @JsonProperty("recipient_user_id")
    private UUID recipientUserId;

    @NotNull(message = "type is required")
    private Notification.NotificationType type;

    @NotBlank(message = "title is required")
    @Size(max = 255)
    private String title;

    @Size(max = 4000)
    private String message;

    @JsonProperty("related_entity_type")
    @Size(max = 255)
    private String relatedEntityType;

    @JsonProperty("related_entity_id")
    private UUID relatedEntityId;

    public Notification toEntity() {
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setIsRead(false);
        return notification;
    }
}
