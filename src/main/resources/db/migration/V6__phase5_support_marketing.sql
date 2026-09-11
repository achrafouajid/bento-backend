-- Phase 5: Support & Marketing

CREATE TABLE ticket_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE ticket (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    partner_id UUID NOT NULL REFERENCES partner(id),
    assigned_to_user_id UUID REFERENCES app_user(id),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20),
    ticket_type_id UUID REFERENCES ticket_type(id),
    deadline DATE,
    resolution TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_ticket_org_status ON ticket(organization_id, status);
CREATE INDEX idx_ticket_partner ON ticket(organization_id, partner_id);

CREATE TABLE ticket_comment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    ticket_id UUID NOT NULL REFERENCES ticket(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES app_user(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE campaign (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    title VARCHAR(255) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    template_id UUID REFERENCES proposal_template(id),
    target_tag_id UUID REFERENCES tag(id),
    target_filter JSONB,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    sent_count BIGINT,
    metrics JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_campaign_org_status ON campaign(organization_id, status);
