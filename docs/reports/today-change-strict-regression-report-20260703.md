# 2026-07-03 当日改动严格回归测试证据报告

## 1. 结论总览
- 功能可用性：通过。项目类型、工作计划 4 位小数、任务工时项目类型冗余与 URL 过滤、任务工时导入重复规则均完成真实接口回归。
- 本次改动质量：通过。后端重新 install 并重启后，接口冒烟脚本结果为 `PASS`；前端任务工时关键路径 E2E 为 `3 passed`；生产构建通过。
- 全局门禁：有一个存量 lint 问题需单独处理，`frontend/config/routes.js` 使用 `process` 触发 `no-undef`。本次改动页面目录 lint 通过，生产构建通过。

## 2. 测试时间与环境口径
- 测试时间：2026-07-03 20:21:41 CST
- 后端目录：`/Users/shibin/code/hjy/zhsw/test4/backend/vortex-test-webboot`
- 前端目录：`/Users/shibin/code/hjy/zhsw/test4/frontend`
- 后端 base URL：`http://127.0.0.1:16666/cloud/sample`
- 前端 URL：`http://127.0.0.1:8000`
- 联调用租户：`8c260832be0b4064a1762211821290bf`
- 联调用用户：`6fde9c3c35f7eb2754b5bf045604a3b6`
- 联调用 token：`ca79****00f4a`
- 服务生效确认：先发现旧 webboot 依赖导致任务工时导入仍走旧逻辑；随后执行 webboot 聚合 install，再用 `mvn -s /Users/shibin/.m2/settings.xml spring-boot:run` 重启，端口 `16666` 重新提供服务后复测通过。

## 3. 执行记录
| 命令 | 结果 | 关键输出摘要 |
| --- | --- | --- |
| `lsof -nP -iTCP -sTCP:LISTEN \| egrep '(:16666\|:8080\|:9527\|:5173\|:3000\|:80[0-9]{2})' \|\| true` | 通过 | 冒烟前识别后端 `16666` 进程状态 |
| `mvn -s /Users/shibin/.m2/settings.xml spring-boot:run` | 先失败后重启通过 | 第一次复用到旧 install 产物，任务工时导入重复规则仍表现为旧逻辑；重新 install 后再次启动通过 |
| `mvn -s /Users/shibin/.m2/settings.xml -pl vortex-test-webboot -am -DskipTests install` | 通过 | `BUILD SUCCESS`，确认 webboot 及依赖模块安装为最新代码 |
| `POST /taskWorkItem/delete?ids=2073017855691935745,2073017855700324353` | 通过 | 清理旧包失败验证时插入的两条测试任务 |
| 当日后端回归脚本，产物目录 `scripts/artifacts/today-regression-20260703-201819` | 通过 | `summary.env` 记录 `result=PASS`，详细结果见 `results.json` |
| `pnpm start` | 通过 | Umi dev server 监听 `http://localhost:8000`，`/cloud/sample` 代理到 `http://localhost:16666/` |
| `E2E_BASE_URL=http://127.0.0.1:8000 pnpm e2e:taskWorkItem` | 通过 | `3 passed (6.8s)` |
| `pnpm exec eslint src/pages/project src/pages/workPlan src/pages/taskWorkItem --ext .js,.jsx` | 通过 | 仅有 React version 配置 warning，无错误 |
| `pnpm exec eslint src/pages/project src/pages/workPlan src/pages/taskWorkItem config/routes.js --ext .js,.jsx` | 失败，存量问题 | `frontend/config/routes.js:30:20 'process' is not defined no-undef` |
| `pnpm build` | 通过 | `Compiled successfully in 5.58s`，生成 `p__taskWorkItem__index`、`p__workPlan__index`、`p__project__index` 等产物 |

## 4. 后端接口冒烟记录
- 项目管理：
  - `project/list?type=PROJECT` 返回 `count=1474`，抽样均为 `PROJECT`。
  - `project/list?type=PRODUCT` 返回 `count=20`，抽样均为 `PRODUCT`。
  - 新增 `PROJECT`、`PRODUCT` 项目后，分页返回 `typeName=项目/产品`。
  - `PRODUCT` 查询能包含产品测试项目，并排除项目测试项目。
  - 导出接口返回 HTTP 200，文件大小约 `5578` bytes。
  - 导入 `类型=产品` 成功，落库后返回 `type=PRODUCT`、`typeName=产品`。
- 工作计划管理：
  - 新增保存 4 位小数通过：`1.2345`、`10.0001`。
  - 新增保存 5 位小数失败，返回 `firstQuarterPersonTimes: 一季度（人/次）最多保留4位小数`。
  - 导入 Excel 浮点尾差值 `84.65000000000001` 成功，分页查询归一为 `84.65`。
  - 导出接口返回 HTTP 200，文件大小约 `4460` bytes。
- 任务工时管理：
  - 使用产品项目新增任务后，分页返回 `projectType=PRODUCT`、`projectTypeName=产品`。
  - `projectType=PRODUCT` 查询包含产品任务；`projectType=PROJECT` 查询排除该产品任务。
  - 使用项目类型项目新增任务后，分页返回 `projectType=PROJECT`、`projectTypeName=项目`。
  - 产品过滤导出返回 HTTP 200，文件大小约 `4749` bytes。
  - 同一个 Excel 内部 7 字段重复导入失败：`{"result":1,"msg":"导入失败","data":"af3968e2b85043a1a78f4d223ce6a69d.xlsx"}`。
  - 导入与数据库已有任务相同的数据成功：`{"result":0,"msg":"导入成功","data":1}`，随后分页查询确认同 key 任务数量为 2，证明已移除数据库 7 字段对比与覆盖更新逻辑。
- 清理：
  - 已清理本轮创建的任务、工作计划、项目测试数据。
  - 清理 ID 记录见 `scripts/artifacts/today-regression-20260703-201819/created.json` 与各 `cleanup_*.json`。

## 5. 前端关键路径冒烟记录
- 启动命令：`pnpm start`
- 访问地址：`http://127.0.0.1:8000`
- E2E 命令：`E2E_BASE_URL=http://127.0.0.1:8000 pnpm e2e:taskWorkItem`
- 执行结果：通过，`3 passed (6.8s)`。
- 覆盖路径：
  - `/#/taskWorkItem` 页面展示“任务工时管理”，查询、新增弹窗、导入弹窗、导出弹窗可打开。
  - `/#/taskWorkItem/occupancy` 页面展示“人员资源占用表”，查询、重置、导出弹窗可用。
  - `/#/taskWorkItem/projectOccupancy` 页面展示“项目资源占用表”，查询、重置、导出弹窗可用。
- 页面目录 lint：`project`、`workPlan`、`taskWorkItem` 目录通过。
- 生产构建：通过，说明路由和页面依赖能被生产配置正常打包。

## 6. 变更清单与契约清单
- 项目管理：
  - 新增必填字段“类型”，枚举 key 为 `PROJECT`、`PRODUCT`，显示值为“项目”“产品”。
  - 查询条件、列表、新增、编辑、查看、导入、导出均已补充项目类型。
- 工作计划管理：
  - 查询年份默认值调整为去年。
  - 一季度、二季度、三季度、四季度人次最长 4 位小数。
  - 后端使用 BigDecimal 校验，导入兼容 Excel/Double 浮点尾差，不放过真实超精度。
- 任务工时管理：
  - 前端展示名从“任务工单管理”调整为“任务工时管理”，后端类名和数据库表名不改。
  - 冗余 `projectType`，保存、更新、导入时从项目管理回填。
  - 菜单 URL 支持 `projectType=PROJECT|PRODUCT`；列表、导出、新增/编辑项目下拉受该参数过滤；导入不限制。
  - 导入模板路径使用相对路径 `./resources/template/任务工时管理导入模板.xlsx`。
- 任务工时导入重复规则：
  - 去掉按租户全量查询历史任务构造 `existMap` 的逻辑。
  - 去掉命中数据库重复时更新的逻辑，导入保存改为新增。
  - 仅校验同一个 Excel 内部重复，唯一 key 为：项目编号、模块、开始日期、结束日期、责任人、任务描述、预计工时。

## 7. 失败归因分层
- 业务问题：重新 install 并重启后未发现本次业务功能失败。
- 环境/生效问题：第一次后端接口冒烟发现 webboot 运行时依赖旧 install 产物，导致任务工时导入重复规则仍是旧逻辑；通过 `vortex-test-webboot -am install` 后重启解决。
- 存量问题：`frontend/config/routes.js` 在独立 eslint 命令下报 `process is not defined`，本次未改该存量配置；页面目录 lint 和生产构建均通过。

## 8. 风险与遗留项
- 任务工时导入现在明确允许与数据库历史任务重复新增；这是本次确认后的业务口径。
- 如果需要把 `projectType` URL 参数在页面 E2E 中做网络断言，可在现有 `taskWorkItem-smoke.spec.js` 上继续加请求参数断言；本轮后端接口已验证过滤生效，前端 E2E 已验证页面关键路径可用。
- `frontend/dist.zip` 因执行生产构建在工作区显示有改动，需提交前确认是否纳入交付产物。
