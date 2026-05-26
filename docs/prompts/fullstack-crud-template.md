# 前后端联动 CRUD + 导入导出提示词模板（严格交付推荐版）

请一次性完成 `{resource}`（`{title}`）模块的前后端联动实现，按“严格交付”标准落地，严格遵循：
- 根规范：`AGENTS.md`
- 后端规范：`backend/AGENTS.md`
- 前端规范：`frontend/AGENTS.md`

【技能要求（必须）】
- 后端默认使用技能：`my-crud-skill-common`
- 若业务明确为普通单表，不使用分表技能
- 前端按现有 frontend CRUD 规范与组件体系实现

【基础信息】
- 模块中文名：`{title}`
- 资源名：`{resource}`
- 交付标准：`严格交付`
- API 前缀：`/cloud/sample`
- 页面路径：`frontend/src/pages/{resource}`

【字段定义】
- `{field1}`（示例：`code（编号）：String，必填，最大32个字符，格式 ^[A-Za-z0-9_-]+$，且 tenantId + code 唯一`）
- `{field2}`（示例：`name（名称）：String，必填，最大100个字符`）
- `{field3}`（示例：`tlId（TL人员ID）：String，必填，最大64个字符；下拉选择值（关联 staff）`）
- `{field4}`（示例：`tlName（TL名称）：String，后端回填冗余字段，最大50个字符`）

【下拉/关联字段约束（按需保留）】
- 关联接口：`{relationApi}`（示例：`/cloud/ums/staff/mini/getStaffListByConditions.smvc`）
- 前端提交关联ID，列表与查看展示关联名称
- 后端保存/更新时必须校验关联ID有效性并回填关联名称
- 任一失败场景必须返回可读 `msg`，前端必须透出 `msg`

【后端实现目标】
- 完整实现：`page/list/get/save/update/delete/importExcel/exportExcel`
- QueryDTO 中新增查询字段必须在 buildQuery 显式落条件
- page 接口使用 Pageable（`page` 0基、`size`）
- 写操作统一 `@Transactional(rollbackFor = Exception.class)`
- 唯一校验：新增/修改都校验，修改排除自身
- 导入导出遵循“业务可读字段”口径，不包含 `id` 与关联 `id`
- 导入实现采用“批量预取 + 前置校验 + 批量落库”，禁止循环内远程调用/查库/远程调用

【前端实现目标】
- 页面结构：`index.js / model.js / service.js / components(Add/View/Export)`
- 路由注册到 `config/routes.js`
- 查询字段、列表字段与后端字段口径一致
- 新增/编辑共用同一弹窗表单
- 保存、更新、删除、导入、导出失败时优先展示后端 `msg`（`message.error(res.msg || "操作失败")`）

【导入导出固定规则】
- 导入组件固定 `VtxImport2`，单入口，禁止模式分流
- 模板文件路径：`frontend/public/resources/template/{title}导入模板.xlsx`
- `templateURL`：`/resources/template/{title}导入模板.xlsx`
- `errorURL`：`/cloud/sample/common/downloadImportExcel`
- 上传附加参数默认包含 `tenantId`、`userId`，业务参数按需补充
- 导入模板列与导出列使用业务可读字段，不暴露 `id`、关联 `id`
- 导入失败需可见后端 `msg`，并可下载错误文件

【接口与返回契约】
- 前后端字段命名保持一致，避免同义不同名
- 所有业务失败场景必须返回可读 `msg`，前端必须透出
- 若接口路径/入参/出参有调整，必须同步更新前端调用与说明文档
- 返回结构兼容项目既有风格（如 `{ result, msg, data }`）

【测试验收（严格执行）】
- 最低交付=列表查询+新增+编辑+删除+查看+导入/导出可用
- 标准交付=最低交付+关键字段校验+关联下拉联调
- 严格交付=标准交付+关键路径自动化+测试报告落 `docs/reports`
- 页面自动化关键路径默认=打开+查询+编辑+删除确认+导入弹窗+导出弹窗

【测试同步生成与门禁】
- 生代码=生用例（后端单元/接口 + 前端关键E2E），禁止后补
- 字段口径自动化至少覆盖：必填、长度上限、格式、唯一性、关联回填、失败msg透出
- 覆盖率门禁建议：
  - 后端总体：`line >= 70%`、`branch >= 60%`
  - 前端总体：`line >= 60%`、`branch >= 50%`
  - 本次改动核心模块：`line >= 80%`
- 门禁执行时机：分支合并前必须通过，不允许合并后补测

【交付输出清单（必须）】
1. 变更文件清单（前后端分组）
2. 后端接口清单（路径/方法/入参/出参/示例）
3. 前端页面与路由清单
4. 导入字段映射、校验规则、失败处理说明
5. 关联下拉联调说明（请求参数、返回映射、异常处理）
6. 测试执行结果汇总（功能可用性 vs 工程门禁分层）
7. 严格交付测试报告路径（`docs/reports/...`）
