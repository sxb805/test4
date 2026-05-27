#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "${ROOT_DIR}/scripts/lib/common.sh"

usage() {
  echo "usage: $0 --module <moduleName>"
}

MODULE_NAME=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --module)
      MODULE_NAME="${2:-}"
      shift 2
      ;;
    *)
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$MODULE_NAME" ]]; then
  usage
  exit 1
fi

MODULE_FILE="${ROOT_DIR}/scripts/modules/${MODULE_NAME}.sh"
if [[ ! -f "$MODULE_FILE" ]]; then
  log_error "module config not found: ${MODULE_FILE}"
  exit 1
fi

# shellcheck disable=SC1090
source "$MODULE_FILE"
: "${MODULE_KEY:?MODULE_KEY is required}"

PIPELINE_ID="${MODULE_KEY}-pipeline-$(now_compact)"
PIPE_DIR="${ARTIFACT_ROOT}/${PIPELINE_ID}"
ensure_dir "$PIPE_DIR"
SUMMARY_FILE="${PIPE_DIR}/summary.env"
COMMAND_LOG="${PIPE_DIR}/commands.log"
: > "$SUMMARY_FILE"
: > "$COMMAND_LOG"
export COMMAND_LOG

run_step() {
  local name="$1"
  shift
  local out="${PIPE_DIR}/${name}.out.log"
  local err="${PIPE_DIR}/${name}.err.log"
  local cmd_str="$*"
  log_info "step=${name}"
  if "$@" >"$out" 2>"$err"; then
    printf '%s|%s|PASS|%s\n' "$(now_iso)" "$name" "$cmd_str" >> "$COMMAND_LOG"
    append_kv "$SUMMARY_FILE" "${name}" "PASS"
  else
    local code=$?
    printf '%s|%s|FAIL(%s)|%s\n' "$(now_iso)" "$name" "$code" "$cmd_str" >> "$COMMAND_LOG"
    append_kv "$SUMMARY_FILE" "${name}" "FAIL(${code})"
    log_error "step failed: ${name}, code=${code}"
    exit "$code"
  fi
}

run_step precheck scripts/env-precheck.sh
run_step restart_backend scripts/backend-restart-webboot.sh
run_step auth_login scripts/auth-login.sh

AUTH_ENV="$(awk '/auth ready:/{print $NF}' "${PIPE_DIR}/auth_login.out.log" | tail -n1 || true)"
if [[ -n "$AUTH_ENV" && -f "$AUTH_ENV" ]]; then
  # shellcheck disable=SC1090
  source "$AUTH_ENV"
fi

run_step api_smoke env AUTH_TOKEN="${AUTH_TOKEN:-}" TENANT_ID="${TENANT_ID:-}" USER_ID="${USER_ID:-}" STAFF_ID="${STAFF_ID:-}" scripts/module-api-smoke.sh --module "$MODULE_NAME"

if [[ -n "${MODULE_E2E_CMD:-}" ]]; then
  # shellcheck disable=SC2086
  run_step e2e_smoke bash -lc "${MODULE_E2E_CMD}"
else
  append_kv "$SUMMARY_FILE" "e2e_smoke" "SKIP(no MODULE_E2E_CMD)"
fi

latest_api_dir="$(ls -1dt ${ARTIFACT_ROOT}/${MODULE_KEY}-smoke-* 2>/dev/null | head -n1 || true)"
if [[ -n "$latest_api_dir" && -d "$latest_api_dir" ]]; then
  run_step gen_report scripts/gen-delivery-report.sh "$latest_api_dir"
else
  append_kv "$SUMMARY_FILE" "gen_report" "SKIP(no smoke artifact)"
fi

append_kv "$SUMMARY_FILE" "module" "$MODULE_NAME"
append_kv "$SUMMARY_FILE" "pipeline_id" "$PIPELINE_ID"
append_kv "$SUMMARY_FILE" "result" "PASS"

log_info "pipeline done: ${PIPE_DIR}"
cat "$SUMMARY_FILE"
