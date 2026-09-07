-- V10__seed_dev_data.sql unconditionally INSERTs a well-known admin account
-- (admin@bento.dev / password123) into every environment, production included,
-- and the BCrypt hash for that password is committed in the repository. That is
-- a remotely reachable backdoor: anyone who has seen the source can authenticate.
--
-- Flyway checksums already-applied migrations, so V10 cannot be edited in place
-- without breaking `flyway validate` on every database where it has run. This is
-- the fix-forward: neutralise the account wherever it still carries the seeded
-- hash -- meaning nobody has deliberately taken it over for real use.
--
--   * password_hash is set to a value that is not a valid BCrypt digest, so
--     BCryptPasswordEncoder.matches() returns false for every possible input.
--   * is_active is cleared; the refresh-token path also rejects inactive users,
--     so any existing session on this account dies at its next refresh.
--
-- The row is kept rather than deleted because other tables reference app_user(id)
-- with RESTRICT foreign keys (audit authorship, created_by / assigned_by columns)
-- and a DELETE could fail mid-migration.
--
-- To turn this into a genuine admin account instead, set a fresh password and
-- re-activate it; the hash guard below will no longer match, so this migration
-- will not run again against it.

UPDATE app_user
SET password_hash = 'disabled-dev-seed-account-no-login',
    is_active = false,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@bento.dev'
  AND password_hash = '$2a$12$c4vqkhHazXcwsLLfaKKEWeAhelYkhjokOSvjokQnPsB8/Bo4KnAwq';
