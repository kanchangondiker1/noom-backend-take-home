#!/usr/bin/env bash
#
# Smoke-tests the Sleep API end to end against a running instance.
#
# Usage:
#   ./scripts/test-api.sh                 # against http://localhost:8080
#   BASE_URL=http://host:8080 ./scripts/test-api.sh
#
# Assumes the stack is up (docker-compose up) and the demo user has been seeded by Flyway.
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
API="${BASE_URL}/api/sleep-logs"
USER_ID="11111111-1111-1111-1111-111111111111"

# Pretty-print JSON if jq is available, otherwise pass through untouched.
pp() { if command -v jq >/dev/null 2>&1; then jq .; else cat; fi; }

# curl helper that prints the HTTP status followed by the (pretty) body.
call() {
  local method="$1"; shift
  local url="$1"; shift
  echo "\$ ${method} ${url}"
  curl -sS -X "${method}" "${url}" \
    -H "Content-Type: application/json" \
    -w $'\nHTTP %{http_code}\n' "$@"
  echo
}

echo "=== 1) Create last night's sleep log (requirement #1) ==="
call POST "${API}" \
  -H "X-User-Id: ${USER_ID}" \
  -d '{
        "bedTime":  "2024-03-14T23:15:00",
        "wakeTime": "2024-03-15T07:00:00",
        "feeling":  "GOOD"
      }'

echo "=== 2) Fetch last night's sleep (requirement #2) ==="
call GET "${API}/last-night" -H "X-User-Id: ${USER_ID}"

echo "=== 3) 30-day averages (requirement #3) ==="
call GET "${API}/averages" -H "X-User-Id: ${USER_ID}"

echo "=== 3b) Averages over a custom window (?days=7) ==="
call GET "${API}/averages?days=7" -H "X-User-Id: ${USER_ID}"

echo "=== 4) Error handling: missing X-User-Id -> 400 ==="
call GET "${API}/last-night"

echo "=== 5) Error handling: unknown user -> 404 ==="
call GET "${API}/last-night" -H "X-User-Id: 00000000-0000-0000-0000-000000000000"

echo "=== 6) Error handling: invalid feeling -> 400 ==="
call POST "${API}" \
  -H "X-User-Id: ${USER_ID}" \
  -d '{"bedTime":"2024-03-14T23:00:00","wakeTime":"2024-03-15T07:00:00","feeling":"AMAZING"}'

echo "Done."
