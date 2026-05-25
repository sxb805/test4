# 前后端联动 CRUD + 导入导出提示词模板（填空版）

请一次性完成 `{resource}` 的前后端联动实现，严格遵循：
- 根规范：`AGENTS.md`
- 后端规范：`backend/AGENTS.md`
- 前端规范：`frontend/AGENTS.md`

【基础信息】
- 模块名称（中文）：`{title}`
- 资源名（英文）：`{resource}`
- 交付标准：`{deliveryLevel}`（可选：`最低交付` / `标准交付` / `严格交付`）
- API 前缀：`/cloud/sample`
- 页面路径：`frontend/src/pages/{resource}`

【数据与契约】
- 实体/DTO/VO/Query 字段：`{fields}`
- 查询字段：`{queryFields}`
- 导入口径：`{importContract}`
- 导出列：`{exportContract}`
- 图片字段（可空）：`{imageField}`
- 地图字段（可空）：`{locationField}`
- 状态字段（可空）：`{statusFields}`

【接口目标】
- page/get/save/update/delete/import/export 全链路完成并联调通过。
- page 使用 `Pageable` 语义（0基 page + size）。

【导入固定规则】
1. 前端导入组件固定：`VtxImport2`（单入口，禁止模式分流）。
2. 模板文件由实现方创建：`frontend/public/resources/template/{title}导入模板.xlsx`。
3. `templateURL` 固定指向：`/resources/template/{title}导入模板.xlsx`。
4. `errorURL` 固定：`/cloud/sample/common/downloadImportExcel`。
5. 上传附加参数默认包含 `tenantId/userId`，业务参数按需补充：`{importBizPostData}`。

【交付要求】
1. 后端先明确接口契约，再完成前端页面对齐。
2. 查询字段前后端一致：前端传什么，后端就落什么条件。
3. 导出传完整 `columnJson`，不得回退默认列。
4. 异常提示前后端一致：后端返回可读 `msg`，前端直接透出。
5. 测试按 `{deliveryLevel}` 执行并提交结果：
   - 最低交付：静态检查 + 冒烟
   - 标准交付：最低交付 + 关键单元测试
   - 严格交付：标准交付 + 接口自动化 + 回归清单
6. 若为“严格交付”，必须输出 `docs/reports/` 下可追溯测试报告路径。

【测试同步生成与门禁】
1. 必须：生成功能代码时，同步生成测试代码（后端单元/接口 + 前端页面关键 E2E），禁止只交付功能。
2. 必须：字段口径自动化覆盖“正例 + 反例 + 边界值”，至少包含必填、长度、枚举、格式、唯一性、关联回填。
3. 必须：页面关键 E2E 默认覆盖：打开、查询、编辑、删除确认、导入弹窗、导出弹窗。
4. 覆盖率门禁建议值：
   - 后端总体：`line >= 70%`、`branch >= 60%`
   - 前端总体：`line >= 60%`、`branch >= 50%`
   - 本次改动核心模块：`line >= 80%`
5. 门禁执行时机：分支合并前必过，不允许合并后补测。
