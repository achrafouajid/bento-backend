-- Optional, editable links from tasks and tickets to the record they concern
-- (deal, proposal, customer/prospect, ticket, …).

-- Tasks: the link was already stored, but was required by the API. Drop the requirement and
-- normalise the rows that only ever got half a link written.
ALTER TABLE task ALTER COLUMN related_entity_type DROP NOT NULL;

UPDATE task
SET related_entity_type = NULL
WHERE related_entity_id IS NULL
  AND related_entity_type IS NOT NULL;

-- Tickets: gain the same link pair, and no longer require a partner.
ALTER TABLE ticket ADD COLUMN related_entity_type VARCHAR(50);
ALTER TABLE ticket ADD COLUMN related_entity_id UUID;
ALTER TABLE ticket ALTER COLUMN partner_id DROP NOT NULL;

-- Existing tickets are all partner tickets; carry that over so they keep showing up under
-- their customer once reads move to the generic link.
UPDATE ticket
SET related_entity_type = 'PARTNER',
    related_entity_id = partner_id
WHERE partner_id IS NOT NULL;

-- Supports "everything attached to this record" lookups from a customer/deal/proposal card.
CREATE INDEX idx_task_related_entity ON task(organization_id, related_entity_type, related_entity_id);
CREATE INDEX idx_ticket_related_entity ON ticket(organization_id, related_entity_type, related_entity_id);
