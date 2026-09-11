-- Drop tables backing entities that were never wired to any controller/service
-- (audit finding: DB tables with zero application usage). None of these are
-- referenced by application code; ticket_type_id is an unused column on a
-- table we keep, dropped alongside its now-gone target table.

ALTER TABLE ticket DROP COLUMN IF EXISTS ticket_type_id;

DROP TABLE IF EXISTS partner_activity;
DROP TABLE IF EXISTS partner_fiscal_profile;
DROP TABLE IF EXISTS partner_contact;
DROP TABLE IF EXISTS partner_address;
DROP TABLE IF EXISTS credit_note;
DROP TABLE IF EXISTS recovery_reminder;
DROP TABLE IF EXISTS ticket_type;
DROP TABLE IF EXISTS automation_execution_log;
DROP TABLE IF EXISTS domain_event;
DROP TABLE IF EXISTS webhook_subscription;

-- proposal_template was schema-only (no controller); the frontend already has
-- a working "apply template" feature expecting a `lines` array, so finish
-- wiring this module instead of deleting it.
ALTER TABLE proposal_template ADD COLUMN lines JSONB;
