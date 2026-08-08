-- Phase 3: Sales Pipeline

CREATE TABLE proposal_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    body TEXT,
    channel VARCHAR(20),
    variables JSONB,
    image_file_id VARCHAR(255),
    approval_status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE proposal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id),
    template_id UUID REFERENCES proposal_template(id),
    title VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    delivery_method VARCHAR(100),
    opportunity_value DECIMAL(19, 2),
    closing_probability INTEGER,
    expected_closing_date DATE,
    competitors TEXT[],
    confirmation_method VARCHAR(100),
    confirmation_attachment_file_id VARCHAR(255),
    confirmation_note TEXT,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_proposal_org_partner ON proposal(organization_id, partner_id);

CREATE TABLE proposal_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    proposal_id UUID NOT NULL REFERENCES proposal(id) ON DELETE CASCADE,
    product VARCHAR(255),
    description TEXT,
    qty DECIMAL(19, 2),
    unit_price DECIMAL(19, 2),
    discount DECIMAL(19, 2),
    total DECIMAL(19, 2),
    vendor VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE deal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL REFERENCES partner(id),
    proposal_id UUID REFERENCES proposal(id),
    title VARCHAR(255) NOT NULL,
    stage VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    amount DECIMAL(19, 2),
    discount DECIMAL(19, 2),
    comments TEXT,
    order_number VARCHAR(100),
    order_date DATE,
    requested_delivery_date DATE,
    estimated_delivery_date DATE,
    expected_delivery_date_vendor DATE,
    delivery_date DATE,
    customer_account VARCHAR(100),
    billing_address TEXT,
    delivery_address TEXT,
    contact_person VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    sales_person_user_id UUID REFERENCES app_user(id),
    sales_region VARCHAR(100),
    currency VARCHAR(3),
    payment_terms VARCHAR(100),
    order_total_amount DECIMAL(19, 2),
    vendor_account VARCHAR(100),
    purchase_order_ref VARCHAR(100),
    warehouse_address TEXT,
    transportation_service VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_deal_org_stage ON deal(organization_id, stage);
CREATE INDEX idx_deal_partner ON deal(organization_id, partner_id);

CREATE TABLE deal_order_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    deal_id UUID NOT NULL REFERENCES deal(id) ON DELETE CASCADE,
    product VARCHAR(255),
    description TEXT,
    qty DECIMAL(19, 2),
    unit_price DECIMAL(19, 2),
    discount DECIMAL(19, 2),
    total DECIMAL(19, 2),
    vendor VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_team_id UUID REFERENCES team(id),
    assigned_to_user_id UUID REFERENCES app_user(id),
    assigned_by_user_id UUID NOT NULL REFERENCES app_user(id),
    status VARCHAR(30) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20),
    due_date DATE,
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_task_org_status ON task(organization_id, status);
CREATE INDEX idx_task_assigned_to ON task(organization_id, assigned_to_user_id);

CREATE TABLE task_comment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    task_id UUID NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES app_user(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
