-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create sequences for business numbers
CREATE TABLE business_number_sequence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    prefix VARCHAR(10) NOT NULL,
    next_value BIGINT NOT NULL DEFAULT 1,
    UNIQUE(organization_id, prefix)
);

-- Base organization table (not tenant-filtered itself)
CREATE TABLE organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    logo_url TEXT,
    industry VARCHAR(100),
    timezone VARCHAR(50) DEFAULT 'UTC',
    fiscal_year_start_month INTEGER DEFAULT 1,
    default_currency VARCHAR(3) DEFAULT 'USD',
    plan VARCHAR(20) DEFAULT 'FREE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_organization_created_at ON organization(created_at DESC);
