# 前端 CRUD 生成提示词模板

请基于 `frontend/AGENTS.md` 生成 `{resource}` 页面，要求一次到位并可运行。

已知信息：
- 资源名：`{resource}`
- 中文标题：`{title}`
- 后端 context-path：`/cloud/sample`
- 路由：`/{resource}`
- DTO/VO/Query 字段：
  - `{fields}`
- 导入接口：`/cloud/sample/{resource}/importExcel`
- 导出接口：`/cloud/sample/{resource}/exportExcel`
- 导入模板：`/resources/template/{templateFile}`

强制要求：
1. 页面结构固定生成：`index.js`、`model.js`、`service.js`、`components/Add.js`、`View.js`、`Export.js`。
2. `service.page` 按后端 `Pageable` 规范：GET + `page(0基)` + `size`。
3. 日期字段：回填转 `dayjs`，提交转字符串（`YYYY-MM-DD` / `YYYY-MM-DD HH:mm:ss`）。
4. 保存/更新失败必须弹后端 `msg`。
5. 导出参数必须按 query/form 传递，确保后端 `@RequestParam columnJson` 可接收。
6. 若导入口径与表单口径不一致（如姓名 vs ID），页面必须显式提示。
7. 完成后执行最小自检并汇报：列表、查询、新增、编辑、删除、查看、导入、导出。
