-- Inviting a teammate used to mean creating their AppUser outright with a random password the
-- admin never saw and the invitee was never told, so the account was unreachable. An invitation
-- is the missing intermediate state: a pending record carrying the role/team the admin
-- pre-assigned, which only becomes an AppUser once the invitee follows the emailed link and
-- chooses their own password.
--
-- Only the SHA-256 of the token is stored, matching refresh_token: the plaintext exists once, in
-- the email, so a database leak cannot be replayed into account creation.

CREATE TABLE user_invitation (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID NOT NULL REFERENCES organization(id),
    email            VARCHAR(255) NOT NULL,
    -- Pre-assigned by the inviting admin and applied verbatim when the invitation is accepted;
    -- the invitee never chooses their own role.
    role             VARCHAR(20) NOT NULL,
    team_id          UUID,
    job_title        VARCHAR(100),
    -- A suggested display name the invitee can override on the acceptance form. Nullable
    -- because an admin can invite on an email address alone.
    display_name     VARCHAR(255),
    language         VARCHAR(5) NOT NULL DEFAULT 'en',
    token_hash       VARCHAR(64) NOT NULL UNIQUE,
    -- PENDING | ACCEPTED | REVOKED. Expiry is derived from expires_at rather than stored as a
    -- status, so an expired invitation can be resent without a status transition.
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at      TIMESTAMP WITH TIME ZONE,
    revoked_at       TIMESTAMP WITH TIME ZONE,
    -- The AppUser created on acceptance, so the pending row remains an audit trail of who
    -- joined through which invitation.
    accepted_user_id UUID REFERENCES app_user(id),
    last_sent_at     TIMESTAMP WITH TIME ZONE,
    -- Counts the initial send plus every resend; surfaced in the admin list so a repeatedly
    -- resent invitation reads as "they never got it" rather than "nothing happened".
    send_count       INTEGER NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       UUID,
    updated_by       UUID
);

CREATE INDEX idx_user_invitation_org        ON user_invitation(organization_id, status);
CREATE INDEX idx_user_invitation_org_email  ON user_invitation(organization_id, lower(email));

-- At most one live invitation per address per organization. Accepted and revoked rows are
-- excluded so the same person can be re-invited after leaving, and so a revoked invitation
-- does not block a fresh one.
CREATE UNIQUE INDEX uq_user_invitation_pending
    ON user_invitation(organization_id, lower(email))
    WHERE status = 'PENDING';
