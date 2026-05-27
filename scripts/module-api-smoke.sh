#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "${ROOT_DIR}/scripts/lib/common.sh"
source "${ROOT_DIR}/scripts/lib/api.sh"
source "${ROOT_DIR}/scripts/lib/evidence.sh"

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
: "${RESOURCE_PATH:?RESOURCE_PATH is required}"
: "${PAGE_FILTER_KEY:?PAGE_FILTER_KEY is required}"

if ! declare -f build_save_payload >/dev/null 2>&1; then
  log_error "module function build_save_payload is required in ${MODULE_FILE}"
  exit 1
fi

RUN_ID="${MODULE_KEY}-smoke-$(now_compact)"
RUN_DIR="${ARTIFACT_ROOT}/${RUN_ID}"
ensure_dir "$RUN_DIR"
SUMMARY_FILE="${RUN_DIR}/summary.env"
COMMAND_LOG="${RUN_DIR}/commands.log"
export COMMAND_LOG
: > "$SUMMARY_FILE"
: > "$COMMAND_LOG"

BASE_API="${BASE_API:-http://127.0.0.1:16666/cloud/sample}"
PROJECT_LIST_PATH="${PROJECT_LIST_PATH:-project/list}"
DEFAULT_STAFF_TREE_URL="${STAFF_TREE_URL:-http://127.0.0.1:8000/cloud/management/rest/tree/loadStaffTree}"

if [[ -z "${AUTH_TOKEN:-}" || -z "${TENANT_ID:-}" || -z "${USER_ID:-}" ]]; then
  log_info "auth env missing, run scripts/auth-login.sh"
  scripts/auth-login.sh >/tmp/${MODULE_KEY}-auth.log
  AUTH_ENV="$(awk '/auth ready:/{print $NF}' /tmp/${MODULE_KEY}-auth.log | tail -n1)"
  if [[ -z "$AUTH_ENV" || ! -f "$AUTH_ENV" ]]; then
    log_error "cannot locate generated auth env"
    exit 1
  fi
  # shellcheck disable=SC1090
  source "$AUTH_ENV"
fi

export AUTH_TOKEN TENANT_ID USER_ID
STAFF_ID="${STAFF_ID:-}"

MODULE_MARKER_PREFIX="${MODULE_MARKER_PREFIX:-冒烟模块}"
MODULE_MARKER="${MODULE_MARKER_PREFIX}-$(date '+%H%M%S')"
TODAY="$(date '+%Y-%m-%d')"

project_body="${RUN_DIR}/project-list.body.json"
project_head="${RUN_DIR}/project-list.headers.txt"
api_request "project-list" GET "${BASE_API}/${PROJECT_LIST_PATH}" "$project_body" "$project_head" || {
  collect_failure_evidence "$RUN_DIR" "project-list-request-failed"
  exit 1
}
assert_result_zero "$project_body" || {
  collect_failure_evidence "$RUN_DIR" "project-list-business-failed"
  exit 1
}
PROJECT_ID="$(json_extract "$project_body" "obj.data && obj.data[0] && obj.data[0].id")"

OWNER_ID="${STAFF_ID}"
if [[ -z "$OWNER_ID" ]]; then
  staff_body="${RUN_DIR}/staff-tree.body.json"
  staff_head="${RUN_DIR}/staff-tree.headers.txt"
  api_request "staff-tree" GET "$DEFAULT_STAFF_TREE_URL" "$staff_body" "$staff_head" || {
    collect_failure_evidence "$RUN_DIR" "staff-tree-request-failed"
    exit 1
  }
  OWNER_ID="$(node -e "const fs=require('fs');const raw=fs.readFileSync(process.argv[1],'utf8');let obj;try{obj=JSON.parse(raw);}catch(e){process.exit(2)};const roots=Array.isArray(obj)?obj:(Array.isArray(obj?.data)?obj.data:[]);function walk(nodes){for(const n of nodes||[]){const attrs=n?.attributes||{};const t=attrs.type||n?.type;if(t==='Staff'&&attrs.id){console.log(attrs.id);return true;}if(walk(n.children||[])) return true;}return false;} if(!walk(roots)){process.exit(3)}" "$staff_body" | head -n1 || true)"
  if [[ -z "$OWNER_ID" ]]; then
    collect_failure_evidence "$RUN_DIR" "staff-tree-no-staff-node"
    exit 1
  fi
fi

save_payload="${RUN_DIR}/save.json"
build_save_payload "$save_payload" "$PROJECT_ID" "$OWNER_ID" "$MODULE_MARKER" "$TODAY"

save_body="${RUN_DIR}/save.body.json"
save_head="${RUN_DIR}/save.headers.txt"
api_request "save" POST "${BASE_API}/${RESOURCE_PATH}/save" "$save_body" "$save_head" "$save_payload" || {
  collect_failure_evidence "$RUN_DIR" "save-request-failed"
  exit 1
}
assert_result_zero "$save_body" || {
  collect_failure_evidence "$RUN_DIR" "save-business-failed"
  exit 1
}

FILTER_VAL="$(node -e "process.stdout.write(encodeURIComponent(process.argv[1]))" "$MODULE_MARKER")"
page_url="${BASE_API}/${RESOURCE_PATH}/page?page=0&size=10&${PAGE_FILTER_KEY}=${FILTER_VAL}"

page_body="${RUN_DIR}/page.body.json"
page_head="${RUN_DIR}/page.headers.txt"
api_request "page" GET "$page_url" "$page_body" "$page_head" || {
  collect_failure_evidence "$RUN_DIR" "page-request-failed"
  exit 1
}
assert_result_zero "$page_body" || {
  collect_failure_evidence "$RUN_DIR" "page-business-failed"
  exit 1
}
TASK_ID="$(json_extract "$page_body" "obj.data && obj.data.rows && obj.data.rows[0] && obj.data.rows[0].id")"

get_body="${RUN_DIR}/get.body.json"
get_head="${RUN_DIR}/get.headers.txt"
api_request "get" GET "${BASE_API}/${RESOURCE_PATH}/get?id=${TASK_ID}" "$get_body" "$get_head" || {
  collect_failure_evidence "$RUN_DIR" "get-request-failed"
  exit 1
}
assert_result_zero "$get_body" || {
  collect_failure_evidence "$RUN_DIR" "get-business-failed"
  exit 1
}

del_body="${RUN_DIR}/delete.body.json"
del_head="${RUN_DIR}/delete.headers.txt"
api_request "delete" POST "${BASE_API}/${RESOURCE_PATH}/delete?ids=${TASK_ID}" "$del_body" "$del_head" || {
  collect_failure_evidence "$RUN_DIR" "delete-request-failed"
  exit 1
}
assert_result_zero "$del_body" || {
  collect_failure_evidence "$RUN_DIR" "delete-business-failed"
  exit 1
}

append_kv "$SUMMARY_FILE" "run_id" "$RUN_ID"
append_kv "$SUMMARY_FILE" "module" "$MODULE_NAME"
append_kv "$SUMMARY_FILE" "module_key" "$MODULE_KEY"
append_kv "$SUMMARY_FILE" "base_api" "$BASE_API"
append_kv "$SUMMARY_FILE" "project_id" "$PROJECT_ID"
append_kv "$SUMMARY_FILE" "owner_id" "$OWNER_ID"
append_kv "$SUMMARY_FILE" "task_id" "$TASK_ID"
append_kv "$SUMMARY_FILE" "marker" "$MODULE_MARKER"
append_kv "$SUMMARY_FILE" "result" "PASS"

log_info "module api smoke pass: ${RUN_DIR}"
cat "$SUMMARY_FILE"
