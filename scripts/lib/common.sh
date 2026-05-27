#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ARTIFACT_ROOT="${ROOT_DIR}/scripts/artifacts"

now_iso() {
  date '+%Y-%m-%d %H:%M:%S'
}

now_compact() {
  date '+%Y%m%d-%H%M%S'
}

log_info() {
  echo "[$(now_iso)] [INFO] $*"
}

log_warn() {
  echo "[$(now_iso)] [WARN] $*"
}

log_error() {
  echo "[$(now_iso)] [ERROR] $*" >&2
}

require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    log_error "missing command: ${cmd}"
    return 1
  fi
}

ensure_dir() {
  mkdir -p "$1"
}

append_kv() {
  local file="$1"
  local key="$2"
  local value="$3"
  printf '%s=%s\n' "$key" "$value" >> "$file"
}

json_get() {
  local file="$1"
  local expr="$2"
  node -e "const fs=require('fs');const data=JSON.parse(fs.readFileSync(process.argv[1],'utf8'));const v=(${expr});if(v===undefined||v===null){process.exit(2)};if(typeof v==='object'){process.stdout.write(JSON.stringify(v));}else{process.stdout.write(String(v));}" "$file"
}

md5_text() {
  local raw="$1"
  if command -v md5 >/dev/null 2>&1; then
    printf '%s' "$raw" | md5 | awk '{print $NF}'
    return 0
  fi
  if command -v md5sum >/dev/null 2>&1; then
    printf '%s' "$raw" | md5sum | awk '{print $1}'
    return 0
  fi
  if command -v openssl >/dev/null 2>&1; then
    printf '%s' "$raw" | openssl md5 | awk '{print $NF}'
    return 0
  fi
  log_error "no md5 command found"
  return 1
}

run_and_capture() {
  local name="$1"
  shift
  local command_log="${COMMAND_LOG:-}"
  local start_ts
  start_ts="$(now_iso)"
  log_info "run(${name}): $*"
  if "$@"; then
    if [[ -n "$command_log" ]]; then
      printf '%s|%s|PASS|%s\n' "$start_ts" "$name" "$*" >> "$command_log"
    fi
    return 0
  fi
  local code=$?
  if [[ -n "$command_log" ]]; then
    printf '%s|%s|FAIL(%s)|%s\n' "$start_ts" "$name" "$code" "$*" >> "$command_log"
  fi
  return "$code"
}

