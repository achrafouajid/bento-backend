package com.bento.crm.invitation.model;

/**
 * Expiry is deliberately not a status: it is derived from {@code expiresAt} at read time, so a
 * lapsed invitation can be resent by extending the deadline rather than resurrecting a row from
 * a terminal state.
 */
public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    REVOKED
}
