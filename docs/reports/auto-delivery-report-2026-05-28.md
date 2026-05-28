# 自动生成执行证据报告（2026-05-28）

## 1. 结论总览
- 功能可用性：待人工补充（可依据下文执行记录快速判定）。
- 本次改动质量：待人工补充。
- 全局门禁：待人工补充。

## 2. 执行记录（命令 + 结果 + 关键摘要）
1. 时间：2026-05-28 11:00:30\n- 步骤：project-list\n- 结果：PASS\n- 命令：`curl -sS -X GET 'http://127.0.0.1:16666/cloud/sample/project/list' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n2. 时间：2026-05-28 11:00:30\n- 步骤：save\n- 结果：PASS\n- 命令：`curl -sS -X POST 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/save' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b' --data-binary '@/Users/shibin/code/hjy/zhsw/test4/scripts/artifacts/taskworkitem-smoke-20260528-110029/save.json'`\n3. 时间：2026-05-28 11:00:30\n- 步骤：page\n- 结果：PASS\n- 命令：`curl -sS -X GET 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/page?page=0&size=10&moduleName=%E5%86%92%E7%83%9F%E6%A8%A1%E5%9D%97-110029' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n4. 时间：2026-05-28 11:00:30\n- 步骤：get\n- 结果：PASS\n- 命令：`curl -sS -X GET 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/get?id=2059832155057217538' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n5. 时间：2026-05-28 11:00:30\n- 步骤：delete\n- 结果：PASS\n- 命令：`curl -sS -X POST 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/delete?ids=2059832155057217538' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n
## 3. 后端接口冒烟记录
- 产物目录：
- 关键响应文件： / 

## 4. 前端关键路径冒烟记录
- 如有 Playwright 产物，请补充  关键摘要。
- `pnpm -C frontend run e2e:taskWorkItem`：PASS，3 passed，覆盖任务工单主流程、人员资源占用表、项目资源占用表。

## 5. 变更清单与契约清单
- 人员资源占用表：原资源占用表重命名，新增导出入口。
- 项目资源占用表：新增独立页面 `/taskWorkItem/projectOccupancy`，统计维度由责任人切换为项目名称。
- 动态表格契约：后端返回 `columns + tableData`，行数据使用 `name` 和周字段扁平输出，前端负责列宽、对齐和标题渲染。
- 导出契约：人员、项目两类资源占用表分别提供 `weeklyOccupancy/exportExcel` 与 `projectWeeklyOccupancy/exportExcel`。

## 6. 失败归因分层
- 业务问题：导出 `columnJson` 首次构造多出一层 `{}`，已修复并复验通过。
- 环境问题：`spring-boot:run` 从 `vortex-test-webboot` 启动时依赖本地仓库 snapshot，需先执行 `vortex-test-webboot -am install` 确认运行态加载最新 controller/service。
- 存量问题：`config/routes.js` 局部 eslint 会命中 `process is not defined`，本次页面目录 lint 已通过，路由通过 E2E 访问验证。

## 7. 本次测试时间与环境口径
- 时间：2026-05-28
- 产物目录：`scripts/artifacts/taskworkitem-pipeline-20260528-110018`、`scripts/artifacts/occupancy-runtime-check-20260528-1107`
- 后端运行态：`http://127.0.0.1:16666/cloud/sample`，最终生效确认启动时间 `2026-05-28 11:07:05`。

## 8. 资源占用表专项补验
1. `GET /cloud/sample/taskWorkItem/weeklyOccupancy?startDateBegin=2026-01-01&startDateEnd=2026-05-28`：PASS，HTTP 200，`result=0`，`columns=24`，`rows=2`，首列 `field=name/titleTop=责任人`。
2. `GET /cloud/sample/taskWorkItem/projectWeeklyOccupancy?startDateBegin=2026-01-01&startDateEnd=2026-05-28`：PASS，HTTP 200，`result=0`，`columns=24`，`rows=2`，首列 `field=name/titleTop=项目名称`。
3. `POST /cloud/sample/taskWorkItem/weeklyOccupancy/exportExcel`：PASS，HTTP 200，`Content-Type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，文件类型 `Microsoft OOXML`。
4. `POST /cloud/sample/taskWorkItem/projectWeeklyOccupancy/exportExcel`：PASS，HTTP 200，`Content-Type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，文件类型 `Microsoft OOXML`。

## 附录：summary.env
```env
run_id=taskworkitem-smoke-20260528-110029
module=taskWorkItem
module_key=taskworkitem
base_api=http://127.0.0.1:16666/cloud/sample
project_id=2059542062530097153
owner_id=62f77d14531892d50fadf21b81983641
task_id=2059832155057217538
marker=冒烟模块-110029
result=PASS
```
