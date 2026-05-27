#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "${ROOT_DIR}/scripts/lib/common.sh"

OUTPUT_DIR="${ARTIFACT_ROOT}/auth-login-$(now_compact)"
ensure_dir "${OUTPUT_DIR}"
SUMMARY_FILE="${OUTPUT_DIR}/summary.env"
COMMAND_LOG="${OUTPUT_DIR}/commands.log"
export COMMAND_LOG
: > "$SUMMARY_FILE"
: > "$COMMAND_LOG"

BASE_URL="${E2E_BASE_URL:-${E2E_BASE:-http://127.0.0.1:8000}}"
LOGIN_URL="${LOGIN_URL:-${BASE_URL}/cas/login}"
USERNAME="${E2E_USERNAME:-zhsw_test}"
PASSWORD="${E2E_PASSWORD:-Vortex2026Info}"

require_cmd curl
require_cmd node

pass_md5="$(md5_text "$PASSWORD")"
login_payload="${OUTPUT_DIR}/login.json"
cat > "$login_payload" <<JSON
{"username":"${USERNAME}","password":"${pass_md5}"}
JSON

headers_file="${OUTPUT_DIR}/login.headers"
body_file="${OUTPUT_DIR}/login.body.json"

log_info "login via ${LOGIN_URL}, username=${USERNAME}"
if curl -sS -X POST "$LOGIN_URL" -H 'Content-Type: application/json' --data-binary "@${login_payload}" -D "$headers_file" -o "$body_file" --max-time 20; then
  printf '%s|login|PASS|POST %s\n' "$(now_iso)" "$LOGIN_URL" >> "$COMMAND_LOG"
else
  code=$?
  printf '%s|login|FAIL(%s)|POST %s\n' "$(now_iso)" "$code" "$LOGIN_URL" >> "$COMMAND_LOG"
  log_error "login request failed"
  exit "$code"
fi

access_header="$(awk 'BEGIN{IGNORECASE=1} /^access-token:/{sub(/\r$/,"",$0);sub(/^[^:]*:[[:space:]]*/,"",$0);print;exit}' "$headers_file")"
if [[ -z "$access_header" ]]; then
  log_error "missing access-token header"
  exit 1
fi

token="$(node -e "const s=process.argv[1];let o;try{o=JSON.parse(s);}catch(e){process.exit(2)};if(!o.access_token){process.exit(3)};process.stdout.write(String(o.access_token));" "$access_header")"
result="$(node -e "const fs=require('fs');const obj=JSON.parse(fs.readFileSync(process.argv[1],'utf8'));if(obj.result===undefined){process.exit(3)};process.stdout.write(String(obj.result));" "$body_file")"
if [[ "$result" != "0" ]]; then
  log_error "login business failed: result=${result}"
  exit 1
fi

tenant_id="$(node -e "const fs=require('fs');const obj=JSON.parse(fs.readFileSync(process.argv[1],'utf8'));const v=obj?.data?.tenantId;if(!v)process.exit(3);process.stdout.write(String(v));" "$body_file")"
user_id="$(node -e "const fs=require('fs');const obj=JSON.parse(fs.readFileSync(process.argv[1],'utf8'));const v=obj?.data?.userId;if(!v)process.exit(3);process.stdout.write(String(v));" "$body_file")"
staff_id="$(node -e "const fs=require('fs');const obj=JSON.parse(fs.readFileSync(process.argv[1],'utf8'));const v=obj?.data?.staffId||'';process.stdout.write(String(v));" "$body_file")"

append_kv "$SUMMARY_FILE" "base_url" "$BASE_URL"
append_kv "$SUMMARY_FILE" "login_url" "$LOGIN_URL"
append_kv "$SUMMARY_FILE" "username" "$USERNAME"
append_kv "$SUMMARY_FILE" "token" "$token"
append_kv "$SUMMARY_FILE" "tenant_id" "$tenant_id"
append_kv "$SUMMARY_FILE" "user_id" "$user_id"
append_kv "$SUMMARY_FILE" "staff_id" "$staff_id"

ENV_FILE="${OUTPUT_DIR}/auth.env"
cat > "$ENV_FILE" <<ENV
export E2E_BASE_URL='${BASE_URL}'
export AUTH_TOKEN='${token}'
export TENANT_ID='${tenant_id}'
export USER_ID='${user_id}'
export STAFF_ID='${staff_id}'
ENV

log_info "auth ready: ${ENV_FILE}"
cat "$SUMMARY_FILE"
