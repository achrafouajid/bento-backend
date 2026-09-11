-- Phase 4: Operations & Finance

CREATE TABLE purchase_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    deal_id UUID NOT NULL REFERENCES deal(id),
    vendor_partner_id UUID NOT NULL REFERENCES partner(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    delivery_date DATE,
    sent_via VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_purchase_order_org_status ON purchase_order(organization_id, status);

CREATE TABLE purchase_order_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    purchase_order_id UUID NOT NULL REFERENCES purchase_order(id) ON DELETE CASCADE,
    product VARCHAR(255),
    description TEXT,
    qty DECIMAL(19, 2),
    cost DECIMAL(19, 2),
    line_type VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE invoice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    type VARCHAR(20) NOT NULL,
    partner_id UUID NOT NULL REFERENCES partner(id),
    deal_id UUID REFERENCES deal(id),
    purchase_order_id UUID REFERENCES purchase_order(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    due_date DATE,
    sent_at TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    customer_account VARCHAR(100),
    customer_name VARCHAR(255),
    delivery_address TEXT,
    vat_number VARCHAR(50),
    subtotal DECIMAL(19, 2),
    tax DECIMAL(19, 2),
    total DECIMAL(19, 2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_invoice_org_status ON invoice(organization_id, status);
CREATE INDEX idx_invoice_partner ON invoice(organization_id, partner_id);
CREATE INDEX idx_invoice_due_date ON invoice(organization_id, due_date);

CREATE TABLE invoice_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    invoice_id UUID NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    item VARCHAR(255),
    description TEXT,
    qty DECIMAL(19, 2),
    unit_price DECIMAL(19, 2),
    line_type VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE credit_note (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    invoice_id UUID NOT NULL REFERENCES invoice(id),
    partner_id UUID NOT NULL REFERENCES partner(id),
    reason VARCHAR(500),
    amount DECIMAL(19, 2),
    issued_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE recovery_reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    invoice_id UUID NOT NULL REFERENCES invoice(id),
    partner_id UUID NOT NULL REFERENCES partner(id),
    channel VARCHAR(20),
    template_id UUID,
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
