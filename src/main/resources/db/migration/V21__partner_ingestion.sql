-- Bot lead ingestion identity (PC scraper -> POST /partners).
--
-- external_id carries the bot's stable identity for a lead
-- (sha1 of normalized profile_url). source_url keeps the raw profile URL
-- for operators. The partial unique index makes retries idempotent per
-- organization while leaving hand-entered rows (NULL external_id) untouched.

ALTER TABLE partner
    ADD COLUMN external_id VARCHAR(255),
    ADD COLUMN source_url TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_partner_org_external
    ON partner (organization_id, external_id)
    WHERE external_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_partner_org_email
    ON partner (organization_id, lower(email))
    WHERE email IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_partner_source_url
    ON partner (organization_id)
    WHERE source_url IS NOT NULL;
