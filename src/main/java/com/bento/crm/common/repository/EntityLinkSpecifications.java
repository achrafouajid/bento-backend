package com.bento.crm.common.repository;

import com.bento.crm.common.model.EntityLink;
import com.bento.crm.common.model.RelatedEntityType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Reusable predicates for entities that own an {@link EntityLink} under a field named
 * {@code relatedEntity}, so every module filters "records attached to X" identically.
 */
public final class EntityLinkSpecifications {

    /** Field name the owning entities use for their embedded {@link EntityLink}. */
    public static final String LINK_FIELD = "relatedEntity";

    private EntityLinkSpecifications() {
    }

    /** Restricts to a tenant. Always combine this with any other predicate. */
    public static <T> Specification<T> inOrganization(UUID organizationId) {
        return (root, query, cb) -> cb.equal(root.get("organizationId"), organizationId);
    }

    /**
     * Filters on the embedded link. Either half may be {@code null}, in which case that half
     * is not constrained — so {@code relatedTo(DEAL, null)} returns everything attached to any
     * deal, and {@code relatedTo(null, null)} filters nothing.
     */
    public static <T> Specification<T> relatedTo(RelatedEntityType type, UUID entityId) {
        return (root, query, cb) -> {
            var link = root.get(LINK_FIELD);
            if (type != null && entityId != null) {
                return cb.and(
                        cb.equal(link.get("relatedEntityType"), type),
                        cb.equal(link.get("relatedEntityId"), entityId));
            }
            if (type != null) {
                return cb.equal(link.get("relatedEntityType"), type);
            }
            if (entityId != null) {
                return cb.equal(link.get("relatedEntityId"), entityId);
            }
            return cb.conjunction();
        };
    }
}
