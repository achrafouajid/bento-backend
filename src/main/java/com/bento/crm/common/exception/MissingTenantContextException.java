package com.bento.crm.common.exception;

/**
 * Thrown when tenant-scoped code runs outside a request that resolved an organization.
 *
 * <p>It is a programming error rather than a client error — the request either never reached
 * {@link com.bento.crm.common.config.TenantFilterInterceptor} or the work was handed to a thread
 * that does not inherit the context — so it maps to 500, not to a 4xx the caller could act on.
 */
public class MissingTenantContextException extends IllegalStateException {

    public MissingTenantContextException() {
        super("Organization context not initialized for this request");
    }
}
