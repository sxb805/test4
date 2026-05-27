#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

collect_failure_evidence() {
  local run_dir="$1"
  local reason="${2:-unknown}"
  ensure_dir "$run_dir/evidence"

  log_warn "collect failure evidence, reason=${reason}"
  append_kv "$run_dir/summary.env" "failure_reason" "$reason"

  (lsof -nP -iTCP:16666 -sTCP:LISTEN || true) > "$run_dir/evidence/port-16666.txt"
  (lsof -nP -iTCP:8000 -sTCP:LISTEN || true) > "$run_dir/evidence/port-8000.txt"

  local backend_log="${BACKEND_RUN_LOG:-}"
  if [[ -n "$backend_log" && -f "$backend_log" ]]; then
    tail -n 200 "$backend_log" > "$run_dir/evidence/backend-log-tail.txt" || true
  fi

  if [[ -d "${ROOT_DIR}/frontend/test-results" ]]; then
    rm -rf "$run_dir/evidence/frontend-test-results" || true
    cp -R "${ROOT_DIR}/frontend/test-results/." "$run_dir/evidence/frontend-test-results/" 2>/dev/null || true
  fi

  if [[ -d "${ROOT_DIR}/logs" ]]; then
    ls -1t "${ROOT_DIR}/logs" | head -n 5 > "$run_dir/evidence/recent-root-logs.txt" || true
  fi
}
