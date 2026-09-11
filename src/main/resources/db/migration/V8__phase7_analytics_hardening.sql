-- Phase 7: Analytics & Hardening

-- Analytics views and aggregate tables
CREATE TABLE dashboard_metric_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    metric_name VARCHAR(100) NOT NULL,
    metric_value JSONB,
    cached_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(organization_id, metric_name)
);

-- Audit trail for analytics access
CREATE TABLE analytics_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    user_id UUID NOT NULL REFERENCES app_user(id),
    endpoint VARCHAR(255),
    query_params JSONB,
    accessed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Query performance hints
CREATE INDEX idx_dashboard_cache_expiry ON dashboard_metric_cache(expires_at);
CREATE INDEX idx_analytics_audit_org ON analytics_audit_log(organization_id, accessed_at DESC);

-- Add missing indexes for common queries
CREATE INDEX idx_partner_org_stage_created ON partner(organization_id, stage, created_at DESC);
CREATE INDEX idx_deal_org_created ON deal(organization_id, created_at DESC);
CREATE INDEX idx_invoice_org_status_date ON invoice(organization_id, status, due_date);
CREATE INDEX idx_task_org_assigned ON task(organization_id, assigned_to_user_id, status);
CREATE INDEX idx_ticket_org_assigned ON ticket(organization_id, assigned_to_user_id, status);
