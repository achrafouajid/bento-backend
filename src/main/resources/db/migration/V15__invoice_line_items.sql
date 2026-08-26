-- Invoice line items were already sent by the frontend but had no column to persist
-- into, so they were silently dropped. A single invoice's lines are always read/written
-- as a whole with the invoice, so jsonb avoids an unnecessary child table (same
-- convention as partner.company / customer_card.addresses).

ALTER TABLE invoice
    ADD COLUMN lines JSONB NOT NULL DEFAULT '[]'::jsonb;
