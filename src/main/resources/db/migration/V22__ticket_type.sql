-- The Tickets page has always offered a ticket type (Software issue / Broken product /
-- Billing issue) and filtered on it, but the value was never stored: the API had no field
-- for it, so every ticket read back as "N/A". Free text rather than an enum so an
-- organization can grow its own categories.

ALTER TABLE ticket ADD COLUMN type VARCHAR(50);

CREATE INDEX idx_ticket_type ON ticket(organization_id, type);
