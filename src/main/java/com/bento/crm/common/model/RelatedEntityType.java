package com.bento.crm.common.model;

/**
 * The CRM records a {@link EntityLink} can point at.
 *
 * <p>Shared across modules (tasks, tickets, …) so a link created by one module is
 * readable by every other one — the persisted value is the enum name, so adding a
 * constant is backwards compatible while renaming one is not.</p>
 *
 * <p>{@code PARTNER} covers leads, prospects, customers and vendors: they are all rows
 * of {@code partner}, discriminated by {@code partner.type}.</p>
 */
public enum RelatedEntityType {
    PARTNER,
    DEAL,
    PROPOSAL,
    PURCHASE_ORDER,
    INVOICE,
    TICKET,
    CAMPAIGN,
    TASK
}
