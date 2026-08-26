-- Three related gaps closed together, since the partner ledger ("grand livre") needs all of them.
--
-- 1. Invoices carried no human-facing number and no issue date: `created_at` is a row-insert
--    timestamp, not the date the document was issued, and back-dated invoices imported from an
--    ERP would otherwise sort into the wrong place in the ledger.
--
-- 2. Payments had nowhere to live at all. `invoice.paid_at` can only express "fully settled at
--    this instant", so partial settlement, the payment date, and the payment side of the ledger
--    were all unrepresentable. Payments get their own table rather than jsonb on the invoice
--    (unlike `invoice.lines`) because they are queried and aggregated independently of the
--    invoice they settle, and an on-account payment has no invoice at all.
--
-- 3. Deleting a partner was irreversible. `deleted_at` gives a 30-day grace period during which
--    the record is hidden from every list but can still be restored; a scheduled job hard-deletes
--    rows past that window.

ALTER TABLE invoice
    ADD COLUMN invoice_number VARCHAR(64),
    ADD COLUMN invoice_date   DATE;

-- Existing invoices have no issue date of their own; the insert date is the best available
-- approximation and keeps them from rendering with an empty date column in the ledger.
UPDATE invoice SET invoice_date = created_at::date WHERE invoice_date IS NULL;

CREATE INDEX idx_invoice_org_partner ON invoice(organization_id, partner_id);

CREATE TABLE payment (
    id               UUID PRIMARY KEY,
    organization_id  UUID NOT NULL,
    partner_id       UUID NOT NULL,
    -- Nullable: an on-account payment is recorded against the partner before it is allocated
    -- to a specific invoice.
    invoice_id       UUID REFERENCES invoice(id) ON DELETE CASCADE,
    payment_date     DATE NOT NULL,
    -- Always stored positive. Whether a payment reads as a debit or a credit is a property of
    -- the partner side (receivable vs payable), applied when the ledger is composed, so the
    -- stored amount stays unambiguous.
    amount           NUMERIC(19,2) NOT NULL,
    method           VARCHAR(32),
    reference        VARCHAR(128),
    notes            TEXT,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    created_by       UUID,
    updated_by       UUID
);

CREATE INDEX idx_payment_org_partner ON payment(organization_id, partner_id);
CREATE INDEX idx_payment_invoice     ON payment(invoice_id);

ALTER TABLE partner ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_partner_org_deleted ON partner(organization_id, deleted_at);
