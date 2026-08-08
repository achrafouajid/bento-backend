package com.bento.crm.common.context;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> organizationId = new ThreadLocal<>();

    public static void setCurrentOrganizationId(UUID id) {
        organizationId.set(id);
    }

    public static UUID getCurrentOrganizationId() {
        UUID id = organizationId.get();
        if (id == null) {
            throw new IllegalStateException("Organization context not initialized for this request");
        }
        return id;
    }

    public static void clear() {
        organizationId.remove();
    }
}
