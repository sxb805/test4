#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "${ROOT_DIR}/scripts/lib/common.sh"

OUTPUT_DIR="${ARTIFACT_ROOT}/backend-restart-$(now_compact)"
ensure_dir "${OUTPUT_DIR}"
SUMMARY_FILE="${OUTPUT_DIR}/summary.env"
COMMAND_LOG="${OUTPUT_DIR}/commands.log"
LOG_FILE="${OUTPUT_DIR}/webboot.log"
PID_FILE="${OUTPUT_DIR}/webboot.pid"
export COMMAND_LOG
: > "$SUMMARY_FILE"
: > "$COMMAND_LOG"

BACK_PORT="${BACK_PORT:-16666}"
BACK_CONTEXT="${BACK_CONTEXT:-/cloud/sample}"
WEBBOOT_DIR="${ROOT_DIR}/backend/vortex-test-webboot"

require_cmd lsof
require_cmd mvn
require_cmd curl

precheck="$(lsof -nP -iTCP:${BACK_PORT} -sTCP:LISTEN 2>/dev/null || true)"
if [[ -n "$precheck" ]]; then
  echo "$precheck" > "${OUTPUT_DIR}/before-port-${BACK_PORT}.txt"
  pids="$(echo "$precheck" | awk 'NR>1 {print $2}' | sort -u | tr '\n' ' ')"
  if [[ -n "$pids" ]]; then
    log_warn "port ${BACK_PORT} occupied, killing pids: ${pids}"
    for pid in $pids; do
      kill "$pid" 2>/dev/null || true
    done
    sleep 2
    for pid in $pids; do
      if kill -0 "$pid" 2>/dev/null; then
        kill -9 "$pid" 2>/dev/null || true
      fi
    done
  fi
fi

post_kill="$(lsof -nP -iTCP:${BACK_PORT} -sTCP:LISTEN 2>/dev/null || true)"
if [[ -n "$post_kill" ]]; then
  log_error "port ${BACK_PORT} still occupied after kill"
  exit 1
fi

cd "$WEBBOOT_DIR"
log_info "start webboot in ${WEBBOOT_DIR}"
nohup mvn -s /Users/shibin/.m2/settings.xml spring-boot:run > "$LOG_FILE" 2>&1 &
new_pid=$!
echo "$new_pid" > "$PID_FILE"

ready=0
for _ in $(seq 1 90); do
  if grep -q "Undertow started on port ${BACK_PORT}" "$LOG_FILE" 2>/dev/null; then
    ready=1
    break
  fi
  if ! kill -0 "$new_pid" 2>/dev/null; then
    log_error "webboot process exited unexpectedly"
    break
  fi
  sleep 2
done

if [[ "$ready" != "1" ]]; then
  log_error "webboot not ready in time"
  tail -n 200 "$LOG_FILE" > "${OUTPUT_DIR}/webboot-tail.log" || true
  exit 1
fi

health_code="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 8 "http://127.0.0.1:${BACK_PORT}${BACK_CONTEXT}")"
commit="$(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || true)"
started_line="$(grep -m1 "Undertow started on port ${BACK_PORT}" "$LOG_FILE" || true)"

append_kv "$SUMMARY_FILE" "backend_port" "$BACK_PORT"
append_kv "$SUMMARY_FILE" "backend_context" "$BACK_CONTEXT"
append_kv "$SUMMARY_FILE" "startup_pid" "$new_pid"
append_kv "$SUMMARY_FILE" "health_http_code" "$health_code"
append_kv "$SUMMARY_FILE" "git_commit" "$commit"
append_kv "$SUMMARY_FILE" "startup_line" "${started_line//=/\\= }"
append_kv "$SUMMARY_FILE" "log_file" "$LOG_FILE"

printf '%s|backend-restart|PASS|mvn -s /Users/shibin/.m2/settings.xml spring-boot:run\n' "$(now_iso)" >> "$COMMAND_LOG"

log_info "backend ready, pid=${new_pid}, health=${health_code}"
cat "$SUMMARY_FILE"
