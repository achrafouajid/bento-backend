-- Customer card: Moroccan legal/fiscal business-registration record, 1:1 with a partner.
-- Addresses and personnel are always read/written as a whole with the card, so they're
-- stored as jsonb (same convention as partner.company/product_interests) rather than
-- separate child tables.

CREATE TABLE customer_card (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    partner_id UUID NOT NULL UNIQUE REFERENCES partner(id) ON DELETE CASCADE,
    account_id VARCHAR(50),
    record_type VARCHAR(20),
    name VARCHAR(255) NOT NULL,
    search_name VARCHAR(255),
    erp_account VARCHAR(100),
    ice VARCHAR(15),
    if_number VARCHAR(50),
    rc VARCHAR(50),
    rc_city VARCHAR(100),
    tp VARCHAR(50),
    vat_status JSONB NOT NULL DEFAULT '[]'::jsonb,
    org_type VARCHAR(20),
    parent_account_id UUID,
    addresses JSONB NOT NULL DEFAULT '[]'::jsonb,
    main_phone VARCHAR(50),
    corporate_email VARCHAR(255),
    website_url VARCHAR(255),
    personnel JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_customer_card_partner ON customer_card(partner_id);
