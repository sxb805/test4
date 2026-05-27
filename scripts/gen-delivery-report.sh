#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "${ROOT_DIR}/scripts/lib/common.sh"

INPUT_DIR="${1:-}"
if [[ -z "$INPUT_DIR" ]]; then
  log_error "usage: scripts/gen-delivery-report.sh <artifact-run-dir>"
  exit 1
fi

if [[ ! -d "$INPUT_DIR" ]]; then
  log_error "input dir not found: $INPUT_DIR"
  exit 1
fi

DATE_STR="$(date '+%Y-%m-%d')"
OUT_FILE="${ROOT_DIR}/docs/reports/auto-delivery-report-${DATE_STR}.md"

summary_file="${INPUT_DIR}/summary.env"
cmd_file="${INPUT_DIR}/commands.log"

cat > "$OUT_FILE" <<MD
# 自动生成执行证据报告（${DATE_STR}）

## 1. 结论总览
- 功能可用性：待人工补充（可依据下文执行记录快速判定）。
- 本次改动质量：待人工补充。
- 全局门禁：待人工补充。

## 2. 执行记录（命令 + 结果 + 关键摘要）
MD

if [[ -f "$cmd_file" ]]; then
  awk -F'|' '{printf "%d. 时间：%s\\n- 步骤：%s\\n- 结果：%s\\n- 命令：`%s`\\n", NR, $1, $2, $3, $4}' "$cmd_file" >> "$OUT_FILE"
else
  echo "- 未找到 commands.log" >> "$OUT_FILE"
fi

cat >> "$OUT_FILE" <<MD

## 3. 后端接口冒烟记录
- 产物目录：`${INPUT_DIR}`
- 关键响应文件：`*.body.json` / `*.headers.txt`

## 4. 前端关键路径冒烟记录
- 如有 Playwright 产物，请补充 `frontend/test-results` 关键摘要。

## 5. 变更清单与契约清单
- 待人工补充。

## 6. 失败归因分层
- 业务问题：待人工补充。
- 环境问题：待人工补充。
- 存量问题：待人工补充。

## 7. 本次测试时间与环境口径
- 时间：${DATE_STR}
- 产物目录：`${INPUT_DIR}`
MD

if [[ -f "$summary_file" ]]; then
  {
    echo ""
    echo "## 附录：summary.env"
    echo '```env'
    cat "$summary_file"
    echo '```'
  } >> "$OUT_FILE"
fi

log_info "report generated: ${OUT_FILE}"
