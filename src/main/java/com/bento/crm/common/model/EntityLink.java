package com.bento.crm.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Optional polymorphic pointer from a record to the CRM entity it concerns
 * (the deal, proposal, customer/prospect, ticket… a task or ticket was raised for).
 *
 * <p>Embedded rather than a real association because the target is polymorphic: it maps to
 * the {@code related_entity_type}/{@code related_entity_id} column pair on the owning table,
 * which is why owners share this type instead of redeclaring the pair.</p>
 *
 * <p>Always normalised: a link is either fully set or fully empty, so
 * {@code related_entity_type} is never stored without an id (and vice versa).</p>
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityLink {

    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type", length = 50)
    private RelatedEntityType relatedEntityType;

    @Column(name = "related_entity_id", columnDefinition = "uuid")
    private UUID relatedEntityId;

    /**
     * @return the link, or an empty link when either half is missing — callers may pass a
     *         half-filled pair (e.g. a type with no record picked yet in the UI).
     */
    public static EntityLink of(RelatedEntityType type, UUID id) {
        return (type == null || id == null) ? new EntityLink() : new EntityLink(type, id);
    }

    public static EntityLink empty() {
        return new EntityLink();
    }

    public boolean isPresent() {
        return relatedEntityType != null && relatedEntityId != null;
    }

    public boolean pointsTo(RelatedEntityType type) {
        return isPresent() && relatedEntityType == type;
    }

    /** @return the target id when this link points at {@code type}, otherwise {@code null}. */
    public UUID idOf(RelatedEntityType type) {
        return pointsTo(type) ? relatedEntityId : null;
    }
}
