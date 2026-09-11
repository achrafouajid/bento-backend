package com.bento.crm.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.bento.crm.common.model.EntityLink;
import com.bento.crm.common.model.RelatedEntityType;
import jakarta.validation.constraints.AssertTrue;

import java.util.UUID;

/**
 * Contract for create/update requests carrying an optional link to another CRM record.
 *
 * <p>The pair is exposed flat ({@code relatedEntityType} + {@code relatedEntityId}) to keep the
 * wire format stable, and validated here so every module rejects a half-filled link the same way
 * instead of silently persisting a dangling type.</p>
 */
public interface RelatableRequest {

    RelatedEntityType getRelatedEntityType();

    UUID getRelatedEntityId();

    /** Both halves or neither — a type without a target would not resolve to any record. */
    @JsonIgnore
    @AssertTrue(message = "relatedEntityType and relatedEntityId must be provided together")
    default boolean isRelatedEntityConsistent() {
        return (getRelatedEntityType() == null) == (getRelatedEntityId() == null);
    }

    @JsonIgnore
    default EntityLink toEntityLink() {
        return EntityLink.of(getRelatedEntityType(), getRelatedEntityId());
    }
}
