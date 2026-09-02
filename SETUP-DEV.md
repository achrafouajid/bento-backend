# Bento CRM — dev environment setup (one-time)

The dev environment mirrors production on the **same VPS**:

| | Production | Dev |
|---|---|---|
| Frontend | `crmbento.com` | `dev.crmbento.com` |
| Backend API | `api.crmbento.com` | `apidev.crmbento.com` |
| Backend app dir | `/srv/bento/apps/crm-backend` | `/srv/bento/apps/crm-backend-dev` |
| Frontend app dir | `/srv/bento/apps/crm` | `/srv/bento/apps/crm-dev` |
| Branch that deploys here | `main` | `dev` |
| Compose file | `docker-compose.yml` | `docker-compose.dev.yml` |
| Backend containers | `crm-backend`, `crm-postgres`, `crm-redis`, `crm-pgadmin` | `crm-backend-dev`, `crm-postgres-dev`, `crm-redis-dev` |
| Frontend container | `bento-crm` | `bento-crm-dev` |
| Backend image tags | `:<sha>` + `:latest` | `:dev-<sha>` + `:dev` |

Ongoing use after setup: **`git push origin dev` deploys dev, `git push origin main` deploys prod.** No wrapper script — GitHub Actions routes on the branch name.

---

## 1. DNS

Add two A records pointing at the **same VPS IP as production**:

```
dev.crmbento.com     A   <VPS_IP>
apidev.crmbento.com  A   <VPS_IP>
```

If the zone is behind Cloudflare, set both to **DNS only** (grey cloud), matching
the existing `crmbento.com` / `api.crmbento.com` records — Traefik issues the
Let's Encrypt cert over the HTTP-01 challenge and needs to terminate TLS itself.

## 2. Create the dev app dirs and clone the `dev` branch

```bash
sudo mkdir -p /srv/bento/apps/crm-dev /srv/bento/apps/crm-backend-dev
sudo chown "$USER" /srv/bento/apps/crm-dev /srv/bento/apps/crm-backend-dev

git clone -b dev https://github.com/achrafouajid/bento-backend.git /srv/bento/apps/crm-backend-dev
git clone -b dev https://github.com/achrafouajid/bento-crm.git     /srv/bento/apps/crm-dev
```

(Create the `dev` branch first if it does not exist yet: from a clone of each
repo, `git checkout -b dev main && git push -u origin dev`.)

## 3. Backend `.env`

```bash
cd /srv/bento/apps/crm-backend-dev
cp .env.dev.example .env
# edit .env: set fresh DB_PASSWORD, REDIS_PASSWORD, and a JWT_SECRET >= 32 chars.
# These MUST differ from production — the dev stack has its own isolated volumes.
```

The frontend (`/srv/bento/apps/crm-dev`) needs no `.env`; its config is baked
into the image at build time from `src/environments/environment.dev.ts`.

## 4. Preconditions already satisfied by prod

- External Docker network `proxy` — check with `docker network ls | grep proxy`.
- Traefik running with the `letsencrypt` cert resolver and the `websecure`
  entrypoint. It auto-discovers the new `dev.crmbento.com` / `apidev.crmbento.com`
  routers from the dev containers' labels; nothing to configure.
- GitHub Actions secrets `DEPLOY_SSH_HOST`, `DEPLOY_SSH_KEY`,
  `DEPLOY_SSH_KNOWN_HOSTS` — the dev deploy reuses them (same host).

Confirm headroom for one extra Postgres + Redis + JVM + Node container:
`free -m` and `df -h`.

## 5. First deploy

Push any commit to `dev` in each repo (or run the workflow manually on the `dev`
branch from the Actions tab). CI builds the `:dev` image, SSHes in, and runs
`deploy.sh`, which brings the dev stack up and waits on its healthcheck.

## 6. Verify

```bash
curl -I https://dev.crmbento.com                                  # 200, valid cert
curl    https://apidev.crmbento.com/api/v1/actuator/health        # {"status":"UP"}
docker ps --format '{{.Names}}' | grep -E 'crm-backend-dev|crm-postgres-dev|crm-redis-dev|bento-crm-dev'
docker volume ls | grep crm-backend-dev                           # isolated dev volumes
```

In the browser, load `https://dev.crmbento.com`, sign in, and confirm the network
tab shows calls to `https://apidev.crmbento.com/api/v1/...` succeeding.

## Promoting dev → prod

`dev` is kept ahead of `main`. To ship what's on dev:

```bash
git checkout main && git merge --ff-only dev && git push origin main
```
