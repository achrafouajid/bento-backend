-- Phase 9: Lead sub-resources & Deal Activity

ALTER TABLE partner
    ADD COLUMN company JSONB,
    ADD COLUMN product_interests JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN campaigns JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN notes TEXT;

CREATE TABLE lead_contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    job_title VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    website VARCHAR(255),
    linkedin VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_lead_contact_partner ON lead_contact(partner_id);

CREATE TABLE lead_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    summary VARCHAR(500) NOT NULL,
    detail TEXT,
    assigned_to_user_id UUID,
    next_follow_up_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_lead_activity_partner ON lead_activity(partner_id);

CREATE TABLE lead_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by_user_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_lead_status_history_partner ON lead_status_history(partner_id);

CREATE TABLE deal_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    deal_id UUID NOT NULL REFERENCES deal(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INTEGER,
    caller_name VARCHAR(255),
    outcome VARCHAR(255),
    email_from VARCHAR(255),
    email_to VARCHAR(255),
    subject VARCHAR(500),
    body TEXT,
    direction VARCHAR(10),
    title VARCHAR(500),
    attendees JSONB,
    location VARCHAR(255),
    meeting_type VARCHAR(20),
    meeting_link VARCHAR(500),
    recording_link VARCHAR(500),
    duration_text VARCHAR(50),
    author VARCHAR(255),
    content TEXT,
    due_date TIMESTAMP WITH TIME ZONE,
    assigned_to VARCHAR(255),
    status VARCHAR(20),
    summary VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_deal_activity_deal ON deal_activity(deal_id);
