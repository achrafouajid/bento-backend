-- Cross-tenant references made structurally impossible.
--
-- Every foreign key between two tenant-scoped tables (both carry organization_id) is rebuilt as
-- a COMPOSITE key on (organization_id, <fk_column>) that points at the parent's
-- (organization_id, id). Because the child row's own organization_id participates in the key,
-- Postgres will reject any row whose referenced parent lives in a different organization -- the
-- application no longer has to remember to scope these writes, and a service that forgets cannot
-- create a tenant leak.
--
-- Mechanics:
--   * Each referenced parent first gets UNIQUE (organization_id, id). id is already the primary
--     key, so this adds no new uniqueness; it exists only because a composite FK target must
--     have a matching unique constraint.
--   * FKs that previously had ON DELETE CASCADE / SET NULL keep that behaviour. For SET NULL the
--     Postgres 15+ column-list form `SET NULL (<fk_column>)` is used so the cascade nulls only
--     the FK column and not the NOT NULL organization_id.
--   * DROP CONSTRAINT IF EXISTS uses the Postgres default constraint name (<table>_<column>_fkey).
--     A few of these columns never had a FK at all (payment.partner_id, app_user.team_id, some
--     lead_* and whatsapp author columns); there the DROP is a no-op and only the ADD matters.
--
-- Not covered, deliberately:
--   * References to organization(id) itself stay single-column.
--   * refresh_token has no organization_id (it is keyed by user), so its user_id FK is unchanged.
--   * Polymorphic columns (stored_file.owner_entity_id, task/ticket/notification related_entity_id)
--     have no single target table and cannot be constrained.
--   * customer_card.parent_account_id has no existing FK and an ambiguous target; left as-is.
--   * The orphaned join tables group_member / group_message_read / group_meeting_attendee /
--     partner_tag and the orphaned partner_status_history table are unmapped and unwritten by the
--     application; they are not touched here.

-- ---------------------------------------------------------------------------
-- 1. Unique (organization_id, id) on every table used as a composite-FK parent
-- ---------------------------------------------------------------------------
ALTER TABLE app_user           ADD CONSTRAINT uq_app_user_org_id           UNIQUE (organization_id, id);
ALTER TABLE team               ADD CONSTRAINT uq_team_org_id               UNIQUE (organization_id, id);
ALTER TABLE crm_group          ADD CONSTRAINT uq_crm_group_org_id          UNIQUE (organization_id, id);
ALTER TABLE partner            ADD CONSTRAINT uq_partner_org_id            UNIQUE (organization_id, id);
ALTER TABLE tag                ADD CONSTRAINT uq_tag_org_id                UNIQUE (organization_id, id);
ALTER TABLE proposal_template  ADD CONSTRAINT uq_proposal_template_org_id  UNIQUE (organization_id, id);
ALTER TABLE proposal           ADD CONSTRAINT uq_proposal_org_id           UNIQUE (organization_id, id);
ALTER TABLE deal               ADD CONSTRAINT uq_deal_org_id               UNIQUE (organization_id, id);
ALTER TABLE purchase_order     ADD CONSTRAINT uq_purchase_order_org_id     UNIQUE (organization_id, id);
ALTER TABLE invoice            ADD CONSTRAINT uq_invoice_org_id            UNIQUE (organization_id, id);
ALTER TABLE campaign           ADD CONSTRAINT uq_campaign_org_id           UNIQUE (organization_id, id);
ALTER TABLE campaign_recipient ADD CONSTRAINT uq_campaign_recipient_org_id UNIQUE (organization_id, id);
ALTER TABLE wa_conversation    ADD CONSTRAINT uq_wa_conversation_org_id    UNIQUE (organization_id, id);
ALTER TABLE wa_message         ADD CONSTRAINT uq_wa_message_org_id         UNIQUE (organization_id, id);

-- ---------------------------------------------------------------------------
-- 2. Rebuild each cross-tenant FK as a composite key
-- ---------------------------------------------------------------------------

-- identity ------------------------------------------------------------------
ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_team_id_fkey;
ALTER TABLE app_user ADD CONSTRAINT fk_app_user_team
    FOREIGN KEY (organization_id, team_id) REFERENCES team (organization_id, id);

ALTER TABLE team DROP CONSTRAINT IF EXISTS team_lead_user_id_fkey;
ALTER TABLE team ADD CONSTRAINT fk_team_lead_user
    FOREIGN KEY (organization_id, lead_user_id) REFERENCES app_user (organization_id, id);

ALTER TABLE group_message DROP CONSTRAINT IF EXISTS group_message_group_id_fkey;
ALTER TABLE group_message ADD CONSTRAINT fk_group_message_group
    FOREIGN KEY (organization_id, group_id) REFERENCES crm_group (organization_id, id);
ALTER TABLE group_message DROP CONSTRAINT IF EXISTS group_message_author_user_id_fkey;
ALTER TABLE group_message ADD CONSTRAINT fk_group_message_author_user
    FOREIGN KEY (organization_id, author_user_id) REFERENCES app_user (organization_id, id);

ALTER TABLE group_meeting DROP CONSTRAINT IF EXISTS group_meeting_group_id_fkey;
ALTER TABLE group_meeting ADD CONSTRAINT fk_group_meeting_group
    FOREIGN KEY (organization_id, group_id) REFERENCES crm_group (organization_id, id);
ALTER TABLE group_meeting DROP CONSTRAINT IF EXISTS group_meeting_created_by_user_id_fkey;
ALTER TABLE group_meeting ADD CONSTRAINT fk_group_meeting_created_by_user
    FOREIGN KEY (organization_id, created_by_user_id) REFERENCES app_user (organization_id, id);

-- files -------------------------------------------------------------------
ALTER TABLE stored_file DROP CONSTRAINT IF EXISTS stored_file_uploaded_by_user_id_fkey;
ALTER TABLE stored_file ADD CONSTRAINT fk_stored_file_uploaded_by_user
    FOREIGN KEY (organization_id, uploaded_by_user_id) REFERENCES app_user (organization_id, id);

-- partner + lead sub-resources -------------------------------------------
ALTER TABLE partner DROP CONSTRAINT IF EXISTS partner_assigned_to_user_id_fkey;
ALTER TABLE partner ADD CONSTRAINT fk_partner_assigned_to_user
    FOREIGN KEY (organization_id, assigned_to_user_id) REFERENCES app_user (organization_id, id);
ALTER TABLE partner DROP CONSTRAINT IF EXISTS partner_owner_id_fkey;
ALTER TABLE partner ADD CONSTRAINT fk_partner_owner
    FOREIGN KEY (organization_id, owner_id) REFERENCES app_user (organization_id, id);
ALTER TABLE partner DROP CONSTRAINT IF EXISTS partner_converted_from_partner_id_fkey;
ALTER TABLE partner ADD CONSTRAINT fk_partner_converted_from
    FOREIGN KEY (organization_id, converted_from_partner_id) REFERENCES partner (organization_id, id);

ALTER TABLE lead_contact DROP CONSTRAINT IF EXISTS lead_contact_partner_id_fkey;
ALTER TABLE lead_contact ADD CONSTRAINT fk_lead_contact_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id) ON DELETE CASCADE;

ALTER TABLE lead_activity DROP CONSTRAINT IF EXISTS lead_activity_partner_id_fkey;
ALTER TABLE lead_activity ADD CONSTRAINT fk_lead_activity_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id) ON DELETE CASCADE;
ALTER TABLE lead_activity DROP CONSTRAINT IF EXISTS lead_activity_assigned_to_user_id_fkey;
ALTER TABLE lead_activity ADD CONSTRAINT fk_lead_activity_assigned_to_user
    FOREIGN KEY (organization_id, assigned_to_user_id) REFERENCES app_user (organization_id, id);

ALTER TABLE lead_status_history DROP CONSTRAINT IF EXISTS lead_status_history_partner_id_fkey;
ALTER TABLE lead_status_history ADD CONSTRAINT fk_lead_status_history_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id) ON DELETE CASCADE;
ALTER TABLE lead_status_history DROP CONSTRAINT IF EXISTS lead_status_history_changed_by_user_id_fkey;
ALTER TABLE lead_status_history ADD CONSTRAINT fk_lead_status_history_changed_by_user
    FOREIGN KEY (organization_id, changed_by_user_id) REFERENCES app_user (organization_id, id);

ALTER TABLE customer_card DROP CONSTRAINT IF EXISTS customer_card_partner_id_fkey;
ALTER TABLE customer_card ADD CONSTRAINT fk_customer_card_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id) ON DELETE CASCADE;

-- proposals + deals -----------------------------------------------------
ALTER TABLE proposal DROP CONSTRAINT IF EXISTS proposal_partner_id_fkey;
ALTER TABLE proposal ADD CONSTRAINT fk_proposal_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id);
ALTER TABLE proposal DROP CONSTRAINT IF EXISTS proposal_template_id_fkey;
ALTER TABLE proposal ADD CONSTRAINT fk_proposal_template
    FOREIGN KEY (organization_id, template_id) REFERENCES proposal_template (organization_id, id);

ALTER TABLE deal DROP CONSTRAINT IF EXISTS deal_partner_id_fkey;
ALTER TABLE deal ADD CONSTRAINT fk_deal_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id);
ALTER TABLE deal DROP CONSTRAINT IF EXISTS deal_proposal_id_fkey;
ALTER TABLE deal ADD CONSTRAINT fk_deal_proposal
    FOREIGN KEY (organization_id, proposal_id) REFERENCES proposal (organization_id, id);
ALTER TABLE deal DROP CONSTRAINT IF EXISTS deal_sales_person_user_id_fkey;
ALTER TABLE deal ADD CONSTRAINT fk_deal_sales_person_user
    FOREIGN KEY (organization_id, sales_person_user_id) REFERENCES app_user (organization_id, id);

ALTER TABLE deal_activity DROP CONSTRAINT IF EXISTS deal_activity_deal_id_fkey;
ALTER TABLE deal_activity ADD CONSTRAINT fk_deal_activity_deal
    FOREIGN KEY (organization_id, deal_id) REFERENCES deal (organization_id, id) ON DELETE CASCADE;

-- operations + finance ------------------------------------------------------
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS purchase_order_deal_id_fkey;
ALTER TABLE purchase_order ADD CONSTRAINT fk_purchase_order_deal
    FOREIGN KEY (organization_id, deal_id) REFERENCES deal (organization_id, id);
ALTER TABLE purchase_order DROP CONSTRAINT IF EXISTS purchase_order_vendor_partner_id_fkey;
ALTER TABLE purchase_order ADD CONSTRAINT fk_purchase_order_vendor_partner
    FOREIGN KEY (organization_id, vendor_partner_id) REFERENCES partner (organization_id, id);

ALTER TABLE invoice DROP CONSTRAINT IF EXISTS invoice_partner_id_fkey;
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id);
ALTER TABLE invoice DROP CONSTRAINT IF EXISTS invoice_deal_id_fkey;
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_deal
    FOREIGN KEY (organization_id, deal_id) REFERENCES deal (organization_id, id);
ALTER TABLE invoice DROP CONSTRAINT IF EXISTS invoice_purchase_order_id_fkey;
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_purchase_order
    FOREIGN KEY (organization_id, purchase_order_id) REFERENCES purchase_order (organization_id, id);

ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_partner_id_fkey;
ALTER TABLE payment ADD CONSTRAINT fk_payment_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id);
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_invoice_id_fkey;
ALTER TABLE payment ADD CONSTRAINT fk_payment_invoice
    FOREIGN KEY (organization_id, invoice_id) REFERENCES invoice (organization_id, id) ON DELETE CASCADE;

-- tasks ------------------------------------------------------------------
ALTER TABLE task DROP CONSTRAINT IF EXISTS task_assigned_team_id_fkey;
ALTER TABLE task ADD CONSTRAINT fk_task_assigned_team
    FOREIGN KEY (organization_id, assigned_team_id) REFERENCES team (organization_id, id);
ALTER TABLE task DROP CONSTRAINT IF EXISTS task_assigned_to_user_id_fkey;
ALTER TABLE task ADD CONSTRAINT fk_task_assigned_to_user
    FOREIGN KEY (organization_id, assigned_to_user_id) REFERENCES app_user (organization_id, id);
ALTER TABLE task DROP CONSTRAINT IF EXISTS task_assigned_by_user_id_fkey;
ALTER TABLE task ADD CONSTRAINT fk_task_assigned_by_user
    FOREIGN KEY (organization_id, assigned_by_user_id) REFERENCES app_user (organization_id, id);

-- support --------------------------------------------------------------
ALTER TABLE ticket DROP CONSTRAINT IF EXISTS ticket_partner_id_fkey;
ALTER TABLE ticket ADD CONSTRAINT fk_ticket_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id);
ALTER TABLE ticket DROP CONSTRAINT IF EXISTS ticket_assigned_to_user_id_fkey;
ALTER TABLE ticket ADD CONSTRAINT fk_ticket_assigned_to_user
    FOREIGN KEY (organization_id, assigned_to_user_id) REFERENCES app_user (organization_id, id);

-- notifications ------------------------------------------------------------
ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_recipient_user_id_fkey;
ALTER TABLE notification ADD CONSTRAINT fk_notification_recipient_user
    FOREIGN KEY (organization_id, recipient_user_id) REFERENCES app_user (organization_id, id);

-- invitations -----------------------------------------------------------
ALTER TABLE user_invitation DROP CONSTRAINT IF EXISTS user_invitation_team_id_fkey;
ALTER TABLE user_invitation ADD CONSTRAINT fk_user_invitation_team
    FOREIGN KEY (organization_id, team_id) REFERENCES team (organization_id, id);
ALTER TABLE user_invitation DROP CONSTRAINT IF EXISTS user_invitation_accepted_user_id_fkey;
ALTER TABLE user_invitation ADD CONSTRAINT fk_user_invitation_accepted_user
    FOREIGN KEY (organization_id, accepted_user_id) REFERENCES app_user (organization_id, id);

-- marketing / campaigns -------------------------------------------------
ALTER TABLE campaign DROP CONSTRAINT IF EXISTS campaign_template_id_fkey;
ALTER TABLE campaign ADD CONSTRAINT fk_campaign_template
    FOREIGN KEY (organization_id, template_id) REFERENCES proposal_template (organization_id, id);
ALTER TABLE campaign DROP CONSTRAINT IF EXISTS campaign_target_tag_id_fkey;
ALTER TABLE campaign ADD CONSTRAINT fk_campaign_target_tag
    FOREIGN KEY (organization_id, target_tag_id) REFERENCES tag (organization_id, id);

ALTER TABLE campaign_recipient DROP CONSTRAINT IF EXISTS campaign_recipient_campaign_id_fkey;
ALTER TABLE campaign_recipient ADD CONSTRAINT fk_campaign_recipient_campaign
    FOREIGN KEY (organization_id, campaign_id) REFERENCES campaign (organization_id, id) ON DELETE CASCADE;
ALTER TABLE campaign_recipient DROP CONSTRAINT IF EXISTS campaign_recipient_partner_id_fkey;
ALTER TABLE campaign_recipient ADD CONSTRAINT fk_campaign_recipient_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id) ON DELETE CASCADE;
ALTER TABLE campaign_recipient DROP CONSTRAINT IF EXISTS campaign_recipient_conversation_id_fkey;
ALTER TABLE campaign_recipient ADD CONSTRAINT fk_campaign_recipient_conversation
    FOREIGN KEY (organization_id, conversation_id) REFERENCES wa_conversation (organization_id, id)
    ON DELETE SET NULL (conversation_id);

-- whatsapp ------------------------------------------------------------
ALTER TABLE wa_conversation DROP CONSTRAINT IF EXISTS wa_conversation_partner_id_fkey;
ALTER TABLE wa_conversation ADD CONSTRAINT fk_wa_conversation_partner
    FOREIGN KEY (organization_id, partner_id) REFERENCES partner (organization_id, id)
    ON DELETE SET NULL (partner_id);

ALTER TABLE wa_message DROP CONSTRAINT IF EXISTS wa_message_conversation_id_fkey;
ALTER TABLE wa_message ADD CONSTRAINT fk_wa_message_conversation
    FOREIGN KEY (organization_id, conversation_id) REFERENCES wa_conversation (organization_id, id) ON DELETE CASCADE;
ALTER TABLE wa_message DROP CONSTRAINT IF EXISTS wa_message_campaign_id_fkey;
ALTER TABLE wa_message ADD CONSTRAINT fk_wa_message_campaign
    FOREIGN KEY (organization_id, campaign_id) REFERENCES campaign (organization_id, id)
    ON DELETE SET NULL (campaign_id);
ALTER TABLE wa_message DROP CONSTRAINT IF EXISTS wa_message_recipient_id_fkey;
ALTER TABLE wa_message ADD CONSTRAINT fk_wa_message_recipient
    FOREIGN KEY (organization_id, recipient_id) REFERENCES campaign_recipient (organization_id, id)
    ON DELETE SET NULL (recipient_id);
ALTER TABLE wa_message DROP CONSTRAINT IF EXISTS wa_message_sent_by_user_id_fkey;
ALTER TABLE wa_message ADD CONSTRAINT fk_wa_message_sent_by_user
    FOREIGN KEY (organization_id, sent_by_user_id) REFERENCES app_user (organization_id, id);

ALTER TABLE wa_followup DROP CONSTRAINT IF EXISTS wa_followup_conversation_id_fkey;
ALTER TABLE wa_followup ADD CONSTRAINT fk_wa_followup_conversation
    FOREIGN KEY (organization_id, conversation_id) REFERENCES wa_conversation (organization_id, id) ON DELETE CASCADE;
ALTER TABLE wa_followup DROP CONSTRAINT IF EXISTS wa_followup_campaign_id_fkey;
ALTER TABLE wa_followup ADD CONSTRAINT fk_wa_followup_campaign
    FOREIGN KEY (organization_id, campaign_id) REFERENCES campaign (organization_id, id) ON DELETE CASCADE;
ALTER TABLE wa_followup DROP CONSTRAINT IF EXISTS wa_followup_recipient_id_fkey;
ALTER TABLE wa_followup ADD CONSTRAINT fk_wa_followup_recipient
    FOREIGN KEY (organization_id, recipient_id) REFERENCES campaign_recipient (organization_id, id) ON DELETE CASCADE;
ALTER TABLE wa_followup DROP CONSTRAINT IF EXISTS wa_followup_trigger_message_id_fkey;
ALTER TABLE wa_followup ADD CONSTRAINT fk_wa_followup_trigger_message
    FOREIGN KEY (organization_id, trigger_message_id) REFERENCES wa_message (organization_id, id)
    ON DELETE SET NULL (trigger_message_id);
ALTER TABLE wa_followup DROP CONSTRAINT IF EXISTS wa_followup_sent_message_id_fkey;
ALTER TABLE wa_followup ADD CONSTRAINT fk_wa_followup_sent_message
    FOREIGN KEY (organization_id, sent_message_id) REFERENCES wa_message (organization_id, id)
    ON DELETE SET NULL (sent_message_id);

-- ---------------------------------------------------------------------------
-- 3. Indexes on the composite FK columns that lack one.
--    Postgres does not auto-index the referencing side; these keep cascade
--    deletes and tenant-scoped joins from degrading to sequential scans.
--    (Columns already covered by a phase-migration (organization_id, col)
--    index are omitted.)
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_app_user_team              ON app_user (organization_id, team_id);
CREATE INDEX IF NOT EXISTS idx_team_lead_user             ON team (organization_id, lead_user_id);
CREATE INDEX IF NOT EXISTS idx_group_message_author       ON group_message (organization_id, author_user_id);
CREATE INDEX IF NOT EXISTS idx_group_meeting_created_by   ON group_meeting (organization_id, created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_stored_file_uploaded_by    ON stored_file (organization_id, uploaded_by_user_id);
CREATE INDEX IF NOT EXISTS idx_partner_owner              ON partner (organization_id, owner_id);
CREATE INDEX IF NOT EXISTS idx_partner_converted_from     ON partner (organization_id, converted_from_partner_id);
CREATE INDEX IF NOT EXISTS idx_lead_activity_assigned     ON lead_activity (organization_id, assigned_to_user_id);
CREATE INDEX IF NOT EXISTS idx_proposal_template_fk       ON proposal (organization_id, template_id);
CREATE INDEX IF NOT EXISTS idx_deal_proposal              ON deal (organization_id, proposal_id);
CREATE INDEX IF NOT EXISTS idx_purchase_order_deal        ON purchase_order (organization_id, deal_id);
CREATE INDEX IF NOT EXISTS idx_purchase_order_vendor      ON purchase_order (organization_id, vendor_partner_id);
CREATE INDEX IF NOT EXISTS idx_invoice_deal               ON invoice (organization_id, deal_id);
CREATE INDEX IF NOT EXISTS idx_invoice_purchase_order     ON invoice (organization_id, purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_task_assigned_team         ON task (organization_id, assigned_team_id);
CREATE INDEX IF NOT EXISTS idx_task_assigned_by           ON task (organization_id, assigned_by_user_id);
CREATE INDEX IF NOT EXISTS idx_ticket_assigned_to         ON ticket (organization_id, assigned_to_user_id);
CREATE INDEX IF NOT EXISTS idx_campaign_template          ON campaign (organization_id, template_id);
CREATE INDEX IF NOT EXISTS idx_campaign_target_tag        ON campaign (organization_id, target_tag_id);
CREATE INDEX IF NOT EXISTS idx_user_invitation_team       ON user_invitation (organization_id, team_id);
CREATE INDEX IF NOT EXISTS idx_user_invitation_accepted   ON user_invitation (organization_id, accepted_user_id);
CREATE INDEX IF NOT EXISTS idx_wa_message_recipient       ON wa_message (organization_id, recipient_id);
CREATE INDEX IF NOT EXISTS idx_wa_message_sent_by         ON wa_message (organization_id, sent_by_user_id);
CREATE INDEX IF NOT EXISTS idx_wa_followup_recipient      ON wa_followup (organization_id, recipient_id);
CREATE INDEX IF NOT EXISTS idx_wa_followup_trigger_msg    ON wa_followup (organization_id, trigger_message_id);
CREATE INDEX IF NOT EXISTS idx_wa_followup_sent_msg       ON wa_followup (organization_id, sent_message_id);
