-- Phase 8: Production Readiness

-- Add constraints and triggers for data integrity
ALTER TABLE app_user ADD CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'MANAGER', 'SALESPERSON', 'SUPPORT', 'VIEWER'));

-- Add policy for partner status lifecycle
ALTER TABLE partner ADD CONSTRAINT ck_partner_stage CHECK (stage IN (
    'NEW', 'CONTACTED', 'ATTEMPTED_CONTACT', 'MEETING_SCHEDULED', 'QUALIFIED',
    'PROPOSAL_SENT', 'CONFIRMED', 'CUSTOMER', 'LOST', 'DISQUALIFIED'
));

-- Create indexes for common analytical queries
CREATE INDEX idx_partner_score_org ON partner(organization_id, score DESC) WHERE score IS NOT NULL;
CREATE INDEX idx_deal_amount_org ON deal(organization_id, amount DESC) WHERE amount IS NOT NULL;
CREATE INDEX idx_invoice_total_org ON invoice(organization_id, total DESC) WHERE total IS NOT NULL;

-- Performance indexes for foreign key filtering
CREATE INDEX idx_partner_assigned_user ON partner(organization_id, assigned_to_user_id);
CREATE INDEX idx_deal_sales_person ON deal(organization_id, sales_person_user_id);

-- Enable table statistics updates (Postgres)
ANALYZE;
