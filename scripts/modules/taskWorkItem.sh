#!/usr/bin/env bash

MODULE_KEY="taskworkitem"
RESOURCE_PATH="taskWorkItem"
PAGE_FILTER_KEY="moduleName"
MODULE_MARKER_PREFIX="冒烟模块"
PROJECT_LIST_PATH="project/list"
MODULE_E2E_CMD="pnpm -C frontend run e2e:taskWorkItem"

build_save_payload() {
  local output_file="$1"
  local project_id="$2"
  local owner_id="$3"
  local module_marker="$4"
  local today="$5"

  local task_desc="自动冒烟-$(date '+%Y%m%d%H%M%S')"
  cat > "$output_file" <<JSON
{
  "projectId":"${project_id}",
  "ownerTlId":"${owner_id}",
  "moduleName":"${module_marker}",
  "taskDesc":"${task_desc}",
  "startDate":"${today}",
  "endDate":"${today}",
  "estimatedHours":8,
  "ownerId":"${owner_id}",
  "status":"完成",
  "actualFinishDate":"${today}",
  "actualHours":8,
  "actualOwnerId":"${owner_id}",
  "progressNote":"自动化冒烟"
}
JSON
}
