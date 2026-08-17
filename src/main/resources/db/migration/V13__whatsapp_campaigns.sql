-- Phase 10: WhatsApp campaigns, per-recipient delivery state, and J+3 relances.
--
-- Tenancy note: Meta's inbound webhook arrives unauthenticated and carries no
-- organization_id, only a phone_number_id. wa_account is the routing table that
-- maps that id back to a tenant, which is why phone_number_id is globally unique
-- rather than unique-per-org.

CREATE TABLE wa_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL UNIQUE REFERENCES organization(id),
    provider VARCHAR(20) NOT NULL DEFAULT 'MOCK',
    phone_number_id VARCHAR(64) NOT NULL UNIQUE,
    waba_id VARCHAR(64),
    display_phone_number VARCHAR(32),
    access_token TEXT,
    app_secret VARCHAR(255),
    verify_token VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED',
    quality_rating VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- One conversation per (tenant, phone number). Holds the 24h customer service
-- window state and the permanent opt-out flag.
CREATE TABLE wa_conversation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID REFERENCES partner(id) ON DELETE SET NULL,
    phone_e164 VARCHAR(32) NOT NULL,
    last_outbound_at TIMESTAMP WITH TIME ZONE,
    last_inbound_at TIMESTAMP WITH TIME ZONE,
    window_expires_at TIMESTAMP WITH TIME ZONE,
    opted_out_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_wa_conversation_org_phone UNIQUE (organization_id, phone_e164)
);

CREATE INDEX idx_wa_conversation_partner ON wa_conversation(organization_id, partner_id);

-- Campaign configuration for the WhatsApp channel. template_id already exists on
-- campaign but points at proposal_template; Meta templates are referenced by name,
-- so they get their own columns.
ALTER TABLE campaign
    ADD COLUMN template_name VARCHAR(255),
    ADD COLUMN template_lang VARCHAR(10) NOT NULL DEFAULT 'fr',
    ADD COLUMN template_params JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN body_preview TEXT,
    ADD COLUMN followup_enabled BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN followup_delay_days INTEGER NOT NULL DEFAULT 3,
    ADD COLUMN followup_template_name VARCHAR(255),
    ADD COLUMN followup_delay_minutes INTEGER,
    ADD COLUMN launched_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN campaign.followup_delay_minutes IS
    'Test-only override. When set, the relance is scheduled this many minutes out '
    'instead of followup_delay_days, so a 3-day cycle can be exercised in 3 minutes.';

-- Per-recipient campaign state. This is what the CRM renders as campaign status.
CREATE TABLE campaign_recipient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    campaign_id UUID NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES wa_conversation(id) ON DELETE SET NULL,
    phone_e164 VARCHAR(32),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    replied_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    error_code VARCHAR(20),
    error_title VARCHAR(500),
    followup_count INTEGER NOT NULL DEFAULT 0,
    last_followup_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_campaign_recipient UNIQUE (campaign_id, partner_id)
);

CREATE INDEX idx_campaign_recipient_campaign ON campaign_recipient(campaign_id, status);
CREATE INDEX idx_campaign_recipient_conversation ON campaign_recipient(conversation_id);

CREATE TABLE wa_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    conversation_id UUID NOT NULL REFERENCES wa_conversation(id) ON DELETE CASCADE,
    campaign_id UUID REFERENCES campaign(id) ON DELETE SET NULL,
    recipient_id UUID REFERENCES campaign_recipient(id) ON DELETE SET NULL,
    direction VARCHAR(3) NOT NULL,
    wamid VARCHAR(128) UNIQUE,
    message_type VARCHAR(20) NOT NULL DEFAULT 'text',
    body TEXT,
    template_name VARCHAR(255),
    template_params JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    error_code VARCHAR(20),
    error_title VARCHAR(500),
    sequence_step INTEGER,
    sent_by_user_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_wa_message_conversation ON wa_message(conversation_id, created_at DESC);
CREATE INDEX idx_wa_message_campaign ON wa_message(campaign_id);

CREATE TABLE wa_followup (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    conversation_id UUID NOT NULL REFERENCES wa_conversation(id) ON DELETE CASCADE,
    campaign_id UUID NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES campaign_recipient(id) ON DELETE CASCADE,
    trigger_message_id UUID REFERENCES wa_message(id) ON DELETE SET NULL,
    sent_message_id UUID REFERENCES wa_message(id) ON DELETE SET NULL,
    due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sequence_step INTEGER NOT NULL DEFAULT 1,
    state VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    claimed_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Drives the scheduler's claim query.
CREATE INDEX idx_wa_followup_due ON wa_followup(state, due_at);

-- At most one pending relance per (campaign, conversation). Scoping this to the
-- campaign rather than the conversation alone is deliberate: a contact enrolled in
-- two campaigns must still get a relance for each, but sending an agent three
-- messages in one campaign must not queue three relances.
CREATE UNIQUE INDEX uq_wa_followup_pending
    ON wa_followup(campaign_id, conversation_id)
    WHERE state = 'PENDING';
