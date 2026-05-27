#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

CURL_MAX_TIME="${CURL_MAX_TIME:-20}"
CURL_CONNECT_TIMEOUT="${CURL_CONNECT_TIMEOUT:-5}"
CURL_RETRY="${CURL_RETRY:-1}"
CURL_RETRY_DELAY="${CURL_RETRY_DELAY:-1}"

mask_secret() {
  local value="$1"
  local len=${#value}
  if (( len <= 8 )); then
    printf '****'
    return
  fi
  printf '%s****%s' "${value:0:4}" "${value: -4}"
}

api_request() {
  local name="$1"
  local method="$2"
  local url="$3"
  local body_file="$4"
  local header_file="$5"
  local data_file="${6:-}"

  local cmd=(curl -sS -X "$method" "$url" -D "$header_file" -o "$body_file" --connect-timeout "$CURL_CONNECT_TIMEOUT" --max-time "$CURL_MAX_TIME" --retry "$CURL_RETRY" --retry-delay "$CURL_RETRY_DELAY")

  if [[ -n "${AUTH_TOKEN:-}" ]]; then
    cmd+=("-H" "Authorization: Bearer ${AUTH_TOKEN}")
    cmd+=("-H" "token: ${AUTH_TOKEN}")
    cmd+=("-H" "access_token: ${AUTH_TOKEN}")
  fi
  if [[ -n "${TENANT_ID:-}" ]]; then
    cmd+=("-H" "tenantId: ${TENANT_ID}")
  fi
  if [[ -n "${USER_ID:-}" ]]; then
    cmd+=("-H" "userId: ${USER_ID}")
  fi

  if [[ -n "$data_file" ]]; then
    cmd+=("-H" "Content-Type: application/json" --data-binary "@${data_file}")
  fi

  local display_cmd="curl -sS -X ${method} '${url}'"
  if [[ -n "${AUTH_TOKEN:-}" ]]; then
    display_cmd+=" -H 'Authorization: Bearer $(mask_secret "${AUTH_TOKEN}")'"
  fi
  if [[ -n "${TENANT_ID:-}" ]]; then
    display_cmd+=" -H 'tenantId: ${TENANT_ID}'"
  fi
  if [[ -n "${USER_ID:-}" ]]; then
    display_cmd+=" -H 'userId: ${USER_ID}'"
  fi
  if [[ -n "$data_file" ]]; then
    display_cmd+=" --data-binary '@${data_file}'"
  fi
  log_info "api(${name}): ${display_cmd}"

  if "${cmd[@]}"; then
    if [[ -n "${COMMAND_LOG:-}" ]]; then
      printf '%s|%s|PASS|%s\n' "$(now_iso)" "$name" "$display_cmd" >> "$COMMAND_LOG"
    fi
    return 0
  else
    local code=$?
    if [[ -n "${COMMAND_LOG:-}" ]]; then
      printf '%s|%s|FAIL(%s)|%s\n' "$(now_iso)" "$name" "$code" "$display_cmd" >> "$COMMAND_LOG"
    fi
    return "$code"
  fi
}

json_result() {
  local body_file="$1"
  node -e "const fs=require('fs');const p=process.argv[1];const txt=fs.readFileSync(p,'utf8');let obj;try{obj=JSON.parse(txt);}catch(e){console.error('invalid-json');process.exit(3)};if(obj&&Object.prototype.hasOwnProperty.call(obj,'result')){process.stdout.write(String(obj.result));}else{process.exit(4)}" "$body_file"
}

json_extract() {
  local body_file="$1"
  local expr="$2"
  node -e "const fs=require('fs');const p=process.argv[1];const expr=process.argv[2];const obj=JSON.parse(fs.readFileSync(p,'utf8'));const f=new Function('obj', 'return ('+expr+')');const val=f(obj);if(val===undefined||val===null){process.exit(5)};process.stdout.write(typeof val==='object'?JSON.stringify(val):String(val));" "$body_file" "$expr"
}

assert_result_zero() {
  local body_file="$1"
  local got
  got="$(json_result "$body_file" || true)"
  if [[ "$got" != "0" ]]; then
    log_error "expect result=0, got=${got:-<empty>}"
    return 1
  fi
  return 0
}
