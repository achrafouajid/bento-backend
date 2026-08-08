-- Phase 1: Identity & RBAC

-- app_user table
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    initials VARCHAR(5),
    avatar_color VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'SALESPERSON',
    team_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT true,
    phone VARCHAR(20),
    job_title VARCHAR(100),
    language VARCHAR(5) DEFAULT 'en',
    notify_on_lead_assign BOOLEAN,
    notify_on_deal_update BOOLEAN,
    notify_on_mention BOOLEAN,
    dashboard_kpis JSONB,
    role_override_permissions JSONB,
    last_active_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    UNIQUE(organization_id, email)
);

CREATE INDEX idx_app_user_organization_id ON app_user(organization_id);
CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_role ON app_user(organization_id, role);

-- refresh_token table
CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_by_token_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token(expires_at);

-- team table
CREATE TABLE team (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    name VARCHAR(255) NOT NULL,
    department VARCHAR(20),
    description TEXT,
    lead_user_id UUID REFERENCES app_user(id),
    color VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_team_organization_id ON team(organization_id);

-- crm_group table
CREATE TABLE crm_group (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_crm_group_organization_id ON crm_group(organization_id);

-- group_member join table
CREATE TABLE group_member (
    group_id UUID NOT NULL REFERENCES crm_group(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_group_member_group_id ON group_member(group_id);
CREATE INDEX idx_group_member_user_id ON group_member(user_id);

-- group_message table
CREATE TABLE group_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    group_id UUID NOT NULL REFERENCES crm_group(id),
    author_user_id UUID NOT NULL REFERENCES app_user(id),
    content TEXT NOT NULL,
    read_by_user_ids UUID[],
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_group_message_group_id ON group_message(organization_id, group_id);

-- group_message_read join table (normalized from UUID array)
CREATE TABLE group_message_read (
    message_id UUID NOT NULL REFERENCES group_message(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    read_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id)
);

CREATE INDEX idx_group_message_read_message_id ON group_message_read(message_id);

-- group_meeting table
CREATE TABLE group_meeting (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id),
    group_id UUID NOT NULL REFERENCES crm_group(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by_user_id UUID NOT NULL REFERENCES app_user(id),
    attendee_user_ids UUID[],
    meeting_link VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_group_meeting_group_id ON group_meeting(organization_id, group_id);

-- group_meeting_attendee join table (normalized from UUID array)
CREATE TABLE group_meeting_attendee (
    meeting_id UUID NOT NULL REFERENCES group_meeting(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    PRIMARY KEY (meeting_id, user_id)
);

CREATE INDEX idx_group_meeting_attendee_meeting_id ON group_meeting_attendee(meeting_id);
