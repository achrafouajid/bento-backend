#!/usr/bin/env bash
# Deploy script for crm-backend, invoked over SSH by the GitHub Actions deploy key.
# Fast-forwards the checkout (so compose changes and this script stay in sync),
# pulls the image tag GHCR just pushed, recreates changed services, then waits on
# the container's own healthcheck. Non-zero exit fails the Actions job.
set -euo pipefail

APP_DIR="/srv/bento/apps/crm-backend"
CONTAINER="crm-backend"
IMAGE_TAG="${1:?usage: deploy.sh <image-tag> (reads GHCR_LOGIN_TOKEN from env)}"

cd "$APP_DIR"

# Keep docker-compose.yml and this script current. --ff-only fails loudly rather
# than clobbering anything edited by hand on the server.
git fetch --quiet origin main
git pull --ff-only --quiet origin main

if [ -n "${GHCR_LOGIN_TOKEN:-}" ]; then
  echo "$GHCR_LOGIN_TOKEN" | docker login ghcr.io -u "${GHCR_LOGIN_USER:-github-actions}" --password-stdin
fi

export BACKEND_IMAGE_TAG="$IMAGE_TAG"
docker compose pull app

# Full `up -d` so committed changes to postgres/redis/pgadmin are applied too.
# Compose only recreates services whose config or image actually changed, so a
# normal app-only deploy leaves the database and cache untouched.
docker compose up -d

echo "Waiting for $CONTAINER to become healthy..."
for i in $(seq 1 90); do
  status="$(docker inspect -f '{{.State.Health.Status}}' "$CONTAINER" 2>/dev/null || echo missing)"
  case "$status" in
    healthy)
      echo "$CONTAINER healthy after ${i}s"
      docker image prune -f >/dev/null 2>&1 || true
      exit 0
      ;;
    unhealthy)
      echo "ERROR: $CONTAINER reported unhealthy" >&2
      docker logs "$CONTAINER" --tail 80 >&2
      exit 1
      ;;
  esac
  sleep 1
done

echo "ERROR: $CONTAINER did not become healthy within 90s (last status: ${status:-unknown})" >&2
docker logs "$CONTAINER" --tail 80 >&2
exit 1
