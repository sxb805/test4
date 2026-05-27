# 冒烟自动化落地手册（模块配置驱动）

## 1. 已沉淀脚本
1. `scripts/env-precheck.sh`
- 检查 Java/Maven/Node/pnpm/Playwright、私服可达性、关键端口占用。
2. `scripts/backend-restart-webboot.sh`
- 执行 `precheck-kill-16666 -> start-webboot -> wait-ready -> health-check`。
- 强制在 `backend/vortex-test-webboot` 目录启动后端。
3. `scripts/auth-login.sh`
- 执行 `username/password -> md5 -> /cas/login`，导出 `AUTH_TOKEN/TENANT_ID/USER_ID`。
4. `scripts/module-api-smoke.sh --module <moduleName>`
- 模块通用后端冒烟入口，串行执行 `save/page/get/delete`，自动断言 `result=0`，失败自动采证。
5. `scripts/gen-delivery-report.sh`
- 从执行日志生成 `docs/reports/` 报告骨架。
6. `scripts/module-pipeline.sh --module <moduleName>`
- 一键串联：自检 -> 重启 -> 鉴权 -> 后端冒烟 -> 前端 e2e -> 报告生成。
7. 兼容入口（已保留）
- `scripts/taskworkitem-api-smoke.sh`
- `scripts/run-taskworkitem-pipeline.sh`
- 两者内部已转发到通用入口，原命令不需要改。

## 2. 典型执行命令
1. 仅环境自检：`scripts/env-precheck.sh`
2. 仅拿鉴权：`scripts/auth-login.sh`
3. 通用后端冒烟：`scripts/module-api-smoke.sh --module taskWorkItem`
4. 一键全流程：`scripts/module-pipeline.sh --module taskWorkItem`
5. 兼容旧命令：`scripts/run-taskworkitem-pipeline.sh`

## 3. 新模块接入（只加配置，不改主脚本）
1. 新建 `scripts/modules/<moduleName>.sh`
2. 最小配置模板：
```bash
#!/usr/bin/env bash
MODULE_KEY="yourmodule"
RESOURCE_PATH="yourResourcePath"
PAGE_FILTER_KEY="yourFilterField"
PROJECT_LIST_PATH="project/list"
MODULE_E2E_CMD="pnpm -C frontend exec playwright test tests/e2e/yourmodule-smoke.spec.js --reporter=line"

build_save_payload() {
  local output_file="$1"
  local project_id="$2"
  local owner_id="$3"
  local module_marker="$4"
  local today="$5"
  cat > "$output_file" <<JSON
{
  "projectId":"${project_id}",
  "ownerTlId":"${owner_id}",
  "moduleName":"${module_marker}",
  "taskDesc":"自动冒烟",
  "startDate":"${today}",
  "endDate":"${today}",
  "ownerId":"${owner_id}",
  "status":"完成"
}
JSON
}
```
3. 执行：`scripts/module-pipeline.sh --module <moduleName>`

## 4. 产物位置
1. 所有脚本执行产物：`scripts/artifacts/<run-id>/`
2. 自动报告：`docs/reports/auto-delivery-report-YYYY-MM-DD.md`

## 5. 失败证据自动采集
1. 端口占用快照：`port-16666.txt`、`port-8000.txt`
2. 后端关键日志尾部：`backend-log-tail.txt`
3. 前端 Playwright 产物拷贝：`frontend-test-results/`
4. 命令执行记录：`commands.log`
