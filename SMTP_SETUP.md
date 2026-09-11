# Sending invitation email from the Hostinger VPS

The backend sends transactional email (currently just user invitations) through
Hostinger's SMTP relay using the credentials of a real mailbox on your domain.
Nothing is sent until `MAIL_USERNAME` is set — until then the backend logs the
rendered message and moves on, so an unconfigured environment never fails an invite.

There are four things to get right, in this order. Skipping the DNS step is the
usual reason invitations land in spam.

---

## 1. Create the sending mailbox (hPanel)

Hostinger's relay will only accept a `From` address for a mailbox it actually
hosts, so the sender has to exist as a real account.

1. hPanel → **Emails** → select your domain (`crmbento.com`) → **Email Accounts**.
2. **Create email account**: `no-reply@crmbento.com`, with a strong password.
3. Save that password — it is the SMTP password, and it is *not* your Hostinger
   account password.

If the domain has no email plan attached, hPanel will offer to set one up. The
free plan that ships with Hostinger web hosting is enough for invitation volume;
a VPS-only plan has no mail service, in which case use a domain hosted on one of
your web-hosting plans, or an external relay (see *Alternatives* below).

## 2. Add the DNS records that make mail deliverable

Set these on whichever DNS provider is authoritative for `crmbento.com`
(hPanel → **Domains** → **DNS / Nameservers** if that is Hostinger).

| Type  | Name                | Value                                                              |
|-------|---------------------|--------------------------------------------------------------------|
| MX    | `@`                 | `mx1.hostinger.com` (priority 5), `mx2.hostinger.com` (priority 10) |
| TXT   | `@`                 | `v=spf1 include:_spf.mail.hostinger.com ~all`                       |
| TXT   | `hostingermail._domainkey` | the DKIM value hPanel shows under **Emails → DNS settings**   |
| TXT   | `_dmarc`            | `v=DMARC1; p=quarantine; rua=mailto:dmarc@crmbento.com; pct=100`     |

Notes that matter:

- **Copy DKIM from hPanel, do not invent it.** Hostinger generates a key per
  domain and shows the exact record; it is long and must be pasted verbatim.
- **One SPF record per domain.** If a TXT record starting `v=spf1` already
  exists, edit it to add `include:_spf.mail.hostinger.com` rather than adding a
  second — two SPF records is a hard failure, worse than none.
- Start DMARC at `p=none` if you have other systems sending as this domain and
  want to watch reports before enforcing; move to `p=quarantine` once the reports
  are clean.

Verify propagation before testing (records can take up to a few hours):

```bash
dig +short TXT crmbento.com
```

```bash
dig +short TXT hostingermail._domainkey.crmbento.com
```

## 3. Confirm the VPS can reach the relay

Hostinger blocks outbound SMTP on some VPS plans by default, and it is worth
knowing that *before* blaming the application. From the VPS:

```bash
nc -vz smtp.hostinger.com 465
```

A `succeeded!` means you are fine. If it hangs or is refused, try port 587:

```bash
nc -vz smtp.hostinger.com 587
```

If **both** fail, outbound SMTP is blocked on the VPS — open a ticket with
Hostinger support asking them to unblock ports 465/587 for your VPS, stating that
it is for authenticated transactional email from your own application. They
generally do this on request.

This project defaults to **465 with implicit SSL** because 465 is the more
reliably open of the two on Hostinger VPS images. To use 587/STARTTLS instead,
set `MAIL_PORT=587`, `MAIL_SSL=false`, `MAIL_STARTTLS=true`.

Also make sure the firewall allows the *outbound* connection (ufw only filters
inbound by default, so usually nothing to do):

```bash
sudo ufw status verbose
```

## 4. Set the environment variables

Add to `/srv/bento/apps/crm-backend/.env` on the VPS:

```bash
MAIL_HOST=smtp.hostinger.com
MAIL_PORT=465
MAIL_USERNAME=no-reply@crmbento.com
MAIL_PASSWORD=<the mailbox password from step 1>
MAIL_SSL=true
MAIL_STARTTLS=false
MAIL_FROM=no-reply@crmbento.com
MAIL_FROM_NAME=Bento CRM
MAIL_ENABLED=true
INVITATION_ACCEPT_URL=https://crmbento.com/invite/accept
INVITATION_EXPIRY_DAYS=7
```

`MAIL_FROM` must match `MAIL_USERNAME`. A `From` the relay does not host is
rejected outright, and even when a relay allows it, the mismatch fails DMARC
alignment at the recipient.

`INVITATION_ACCEPT_URL` must point at the **frontend** — that is where the
`/invite/accept` route lives — not at `api.crmbento.com`.

Then redeploy so the container picks up the new environment:

```bash
cd /srv/bento/apps/crm-backend && docker compose up -d
```

---

## Verifying it works

Send a real invitation from **Settings → Users → Invite User**, then watch the
backend:

```bash
docker logs -f crm-backend | grep -iE "sent|mail|invitation"
```

- `Sent 'You have been invited to join …' to …` — dispatched successfully.
- `Mail not configured -- skipping …` — `MAIL_USERNAME` is empty or
  `MAIL_ENABLED=false`. The full rendered email, including the accept link, is in
  the log, so you can still complete the flow by hand while debugging.
- `Failed to send …` — the SMTP conversation failed; the exception says why.
  `AuthenticationFailedException` means wrong username/password;
  a timeout means the port is blocked (back to step 3).

To test the relay independently of the application:

```bash
docker run --rm -it alpine sh -c "apk add --no-cache swaks && swaks --to you@example.com --from no-reply@crmbento.com --server smtp.hostinger.com:465 --tls-on-connect --auth LOGIN --auth-user no-reply@crmbento.com"
```

Once mail is flowing, check alignment by sending an invitation to a Gmail address
and using **Show original** — you want `SPF: PASS`, `DKIM: PASS`, `DMARC: PASS`.

## Common failures

| Symptom | Cause |
|---|---|
| Log says "Mail not configured" | `MAIL_USERNAME` unset, or `MAIL_ENABLED=false` |
| `AuthenticationFailedException` | Using the Hostinger *account* password instead of the *mailbox* password |
| Connection timeout on 465 | Outbound SMTP blocked on the VPS — ask support to unblock |
| Mail sends but lands in spam | DKIM/SPF missing or not yet propagated (step 2) |
| `553 sender address rejected` | `MAIL_FROM` is not a mailbox Hostinger hosts |
| Invite link 404s | `INVITATION_ACCEPT_URL` points at the API instead of the frontend |

## Alternatives

Hostinger's relay has modest sending limits (a few hundred messages a day on the
bundled plans) and no delivery analytics. If invitation volume grows, or you add
campaign email, swap in a dedicated provider — only the four `MAIL_*` connection
variables change, no code does:

- **Brevo** — `smtp-relay.brevo.com:587`, generous free tier.
- **Resend** — `smtp.resend.com:465`.
- **Amazon SES** — cheapest at volume, requires leaving the sandbox.

Each still needs its own SPF/DKIM records on the domain.
