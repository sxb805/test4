# 前后端联动 CRUD + 导入导出提示词模板（填空版）

请一次性完成 `{resource}` 的前后端联动实现，严格遵循：
- 根规范：`AGENTS.md`
- 后端规范：`backend/AGENTS.md`
- 前端规范：`frontend/AGENTS.md`

【基础信息】
- 模块名称（中文）：`{title}`
- 资源名（英文）：`{resource}`
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
5. 最后提交最小冒烟结果：列表、查询、新增、编辑、删除、查看、导入、导出、失败提示。
