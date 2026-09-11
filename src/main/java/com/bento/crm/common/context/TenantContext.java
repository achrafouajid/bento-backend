package com.bento.crm.common.context;

import com.bento.crm.common.exception.MissingTenantContextException;

import java.util.UUID;

/**
 * The organization the current request belongs to, resolved once per request by
 * {@link com.bento.crm.common.config.TenantFilterInterceptor} and read by every tenant-scoped
 * query.
 *
 * <p>Held in a ThreadLocal. Virtual threads are enabled and each request still gets its own
 * carrier-independent thread, so this is per-request state; work handed to a different thread
 * (an {@code @Async} send, a scheduler) does <em>not</em> inherit it and must pass the
 * organization explicitly.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> ORGANIZATION_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentOrganizationId(UUID id) {
        ORGANIZATION_ID.set(id);
    }

    /**
     * @throws MissingTenantContextException when called outside a tenant-scoped request. Callers
     *         that legitimately run without one (schedulers, the WhatsApp webhook) should use
     *         {@link #currentOrganizationIdOrNull()} instead of catching this.
     */
    public static UUID getCurrentOrganizationId() {
        UUID id = ORGANIZATION_ID.get();
        if (id == null) {
            throw new MissingTenantContextException();
        }
        return id;
    }

    /** The current organization, or {@code null} outside a tenant-scoped request. */
    public static UUID currentOrganizationIdOrNull() {
        return ORGANIZATION_ID.get();
    }

    public static void clear() {
        ORGANIZATION_ID.remove();
    }
}
