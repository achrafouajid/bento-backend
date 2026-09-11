package com.bento.crm.common.model;

import com.bento.crm.common.context.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.util.UUID;

/**
 * Last line of defence for tenant isolation on the write path.
 *
 * <p>Hibernate's {@code organizationFilter} guards reads, and the repositories take an
 * {@code organizationId} on every finder, but neither constrains a write: a service that forgets
 * to stamp the tenant, or stamps the wrong one, produces a row that silently belongs to somebody
 * else. This listener makes that impossible for any entity persisted inside a request.
 *
 * <p>Outside a request there is no tenant to check against — the WhatsApp webhook and the
 * schedulers resolve their own organization and pass it explicitly — so the checks are skipped
 * rather than failing those paths.
 */
public class TenantEntityListener {

    @PrePersist
    void stampOnInsert(BaseTenantEntity entity) {
        UUID current = currentTenantOrNull();
        if (current == null) {
            if (entity.getOrganizationId() == null) {
                throw new IllegalStateException(
                        entity.getClass().getSimpleName()
                                + " was persisted with no organization, outside any tenant context");
            }
            return;
        }
        if (entity.getOrganizationId() == null) {
            entity.setOrganizationId(current);
        } else if (!current.equals(entity.getOrganizationId())) {
            throw new CrossTenantWriteException(entity, current);
        }
    }

    @PreUpdate
    void verifyOnUpdate(BaseTenantEntity entity) {
        UUID current = currentTenantOrNull();
        if (current != null && !current.equals(entity.getOrganizationId())) {
            throw new CrossTenantWriteException(entity, current);
        }
    }

    private static UUID currentTenantOrNull() {
        return TenantContext.currentOrganizationIdOrNull();
    }

    /** Signals an attempt to write a row into a tenant other than the caller's. */
    public static class CrossTenantWriteException extends SecurityException {
        public CrossTenantWriteException(BaseTenantEntity entity, UUID currentTenant) {
            super("Refusing to write " + entity.getClass().getSimpleName() + " " + entity.getId()
                    + " belonging to organization " + entity.getOrganizationId()
                    + " from a request scoped to organization " + currentTenant);
        }
    }
}
