-- Phase 2: Partner Core

CREATE TABLE partner (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    type VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    record_type VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(20),
    city VARCHAR(100),
    country VARCHAR(100),
    source VARCHAR(20),
    score INTEGER,
    temperature VARCHAR(20),
    priority VARCHAR(20),
    qualification VARCHAR(20),
    stage VARCHAR(50) NOT NULL,
    assigned_to_user_id UUID REFERENCES app_user(id),
    owner_id UUID REFERENCES app_user(id),
    converted_from_partner_id UUID REFERENCES partner(id),
    estimated_deal_value DECIMAL(19, 2),
    probability INTEGER,
    expected_close_date DATE,
    comments TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_partner_org_type ON partner(organization_id, type);
CREATE INDEX idx_partner_stage ON partner(organization_id, stage);
CREATE INDEX idx_partner_assigned ON partner(organization_id, assigned_to_user_id);

CREATE TABLE partner_address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    address_type VARCHAR(30),
    street_address VARCHAR(255),
    industrial_zone VARCHAR(100),
    postal_code VARCHAR(20),
    city VARCHAR(100),
    country VARCHAR(100),
    is_primary BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_partner_address_partner_id ON partner_address(organization_id, partner_id);

CREATE TABLE partner_contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    full_name VARCHAR(255),
    job_title VARCHAR(100),
    mobile VARCHAR(20),
    email VARCHAR(255),
    is_primary BOOLEAN,
    is_decision_maker BOOLEAN,
    is_technical_contact BOOLEAN,
    is_finance_contact BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_partner_contact_partner_id ON partner_contact(organization_id, partner_id);

CREATE TABLE partner_fiscal_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL UNIQUE REFERENCES partner(id) ON DELETE CASCADE,
    account_id VARCHAR(50),
    erp_account VARCHAR(50),
    ice VARCHAR(50),
    if_field VARCHAR(50),
    rc VARCHAR(50),
    rc_city VARCHAR(100),
    tp VARCHAR(50),
    vat_status TEXT[],
    org_type VARCHAR(30),
    parent_account_partner_id UUID REFERENCES partner(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE partner_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    summary VARCHAR(500),
    detail TEXT,
    assigned_to_user_id UUID REFERENCES app_user(id),
    next_follow_up_at TIMESTAMP WITH TIME ZONE,
    occurred_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_partner_activity_partner_id ON partner_activity(organization_id, partner_id);

CREATE TABLE tag (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    name VARCHAR(100) NOT NULL,
    tag_type VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE partner_tag (
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (partner_id, tag_id)
);

CREATE TABLE partner_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id) ON DELETE CASCADE,
    from_stage VARCHAR(50),
    to_stage VARCHAR(50),
    changed_by_user_id UUID REFERENCES app_user(id),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE stored_file (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    owner_entity_id UUID,
    owner_entity_type VARCHAR(50),
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by_user_id UUID REFERENCES app_user(id),
    uploaded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_stored_file_org_owner ON stored_file(organization_id, owner_entity_type, owner_entity_id);
