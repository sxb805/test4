# 前后端联动 CRUD 生成提示词模板

请一次性完成 `{resource}` 的前后端 CRUD + 导入导出，严格遵循：
- 根规范：`AGENTS.md`
- 后端规范：`backend/AGENTS.md`
- 前端规范：`frontend/AGENTS.md`

输入信息：
- 资源名：`{resource}`
- 中文标题：`{title}`
- 后端 context-path：`/cloud/sample`
- 实体/DTO/VO/Query 字段：`{fields}`
- 导入口径：`{importContract}`
- 导出列：`{exportContract}`

交付要求：
1. 后端先完成并明确接口契约，再生成前端页面对齐契约。
2. 前端分页参数必须匹配后端 `Pageable`（0基 page + size）。
3. 查询字段前后端一致：前端传什么，后端就落什么条件。
4. 导入模板必须新建到 `frontend/public/resources/template/`，名称与资源对应，禁止复用他页模板。
5. 导入失败必须前端可见（msg + 错误文件ID）。
6. 导出必须传完整 `columnJson`，不得回退默认2列。
7. 最后按清单逐项自测：列表、查询、新增、编辑、删除、查看、导入、导出、异常提示。
