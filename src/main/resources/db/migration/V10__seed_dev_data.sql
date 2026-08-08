-- Seed development data for testing
-- This migration provides test credentials for frontend development

-- Create default organization if not exists
INSERT INTO organization (id, name, industry, timezone, fiscal_year_start_month, default_currency, created_at, updated_at)
SELECT
    '550e8400-e29b-41d4-a716-446655440000'::uuid as id,
    'Bento Demo' as name,
    'Technology' as industry,
    'UTC' as timezone,
    1 as fiscal_year_start_month,
    'USD' as default_currency,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE id = '550e8400-e29b-41d4-a716-446655440000'::uuid);

-- Create default admin user (email: admin@bento.dev, password: password123)
-- Password hash for 'password123' using BCrypt with 12 rounds: $2a$12$c4vqkhHazXcwsLLfaKKEWeAhelYkhjokOSvjokQnPsB8/Bo4KnAwq
INSERT INTO app_user (id, organization_id, email, password_hash, display_name, role, is_active, created_at, updated_at)
SELECT
    '550e8400-e29b-41d4-a716-446655440001'::uuid as id,
    '550e8400-e29b-41d4-a716-446655440000'::uuid as organization_id,
    'admin@bento.dev' as email,
    '$2a$12$c4vqkhHazXcwsLLfaKKEWeAhelYkhjokOSvjokQnPsB8/Bo4KnAwq' as password_hash,
    'Admin User' as display_name,
    'ADMIN' as role,
    true as is_active,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@bento.dev');
