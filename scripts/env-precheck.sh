#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "${ROOT_DIR}/scripts/lib/common.sh"

OUTPUT_DIR="${ARTIFACT_ROOT}/env-precheck-$(now_compact)"
ensure_dir "${OUTPUT_DIR}"
SUMMARY_FILE="${OUTPUT_DIR}/summary.env"
COMMAND_LOG="${OUTPUT_DIR}/commands.log"
export COMMAND_LOG
: > "${SUMMARY_FILE}"
: > "${COMMAND_LOG}"

PRIVATE_MAVEN_URL="${PRIVATE_MAVEN_URL:-http://maven.cloudhw.cn:8081}"
FRONT_PORT="${FRONT_PORT:-8000}"
BACK_PORT="${BACK_PORT:-16666}"

check_bin() {
  local key="$1"
  local cmd="$2"
  if command -v "$cmd" >/dev/null 2>&1; then
    local version
    version="$($cmd --version 2>/dev/null | head -n 1 || true)"
    append_kv "$SUMMARY_FILE" "${key}_installed" "true"
    append_kv "$SUMMARY_FILE" "${key}_version" "${version}"
  else
    append_kv "$SUMMARY_FILE" "${key}_installed" "false"
  fi
}

check_port() {
  local port="$1"
  local key="$2"
  local lines
  lines="$(lsof -nP -iTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "$lines" ]]; then
    append_kv "$SUMMARY_FILE" "${key}_occupied" "true"
    printf '%s\n' "$lines" > "${OUTPUT_DIR}/${key}-port-${port}.txt"
  else
    append_kv "$SUMMARY_FILE" "${key}_occupied" "false"
  fi
}

check_http() {
  local key="$1"
  local url="$2"
  local code
  code="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 8 "$url" || true)"
  append_kv "$SUMMARY_FILE" "${key}_http_code" "$code"
}

log_info "precheck start, output=${OUTPUT_DIR}"

check_bin java java
check_bin mvn mvn
check_bin node node
check_bin pnpm pnpm

if command -v pnpm >/dev/null 2>&1; then
  if run_and_capture "resolve-playwright-test" bash -lc "cd '${ROOT_DIR}/frontend' && node -e \"require.resolve('@playwright/test');console.log('ok')\"" >"${OUTPUT_DIR}/playwright-test.resolve.log" 2>&1; then
    append_kv "$SUMMARY_FILE" "playwright_test_installed" "true"
  else
    append_kv "$SUMMARY_FILE" "playwright_test_installed" "false"
  fi
fi

check_http private_maven "$PRIVATE_MAVEN_URL"
check_http front_service "http://127.0.0.1:${FRONT_PORT}"
check_http back_service "http://127.0.0.1:${BACK_PORT}/cloud/sample"

check_port "$FRONT_PORT" front
check_port "$BACK_PORT" back

log_info "precheck done"
log_info "summary: ${SUMMARY_FILE}"
cat "$SUMMARY_FILE"
