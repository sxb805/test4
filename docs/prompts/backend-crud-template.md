# 后端 CRUD 生成提示词模板

请基于 `backend/AGENTS.md` 为 `{resource}` 生成后端 CRUD + 导入导出，要求性能和可维护性优先。

已知信息：
- 模块：`{module}`
- 资源名：`{resource}`
- 实体字段：`{fields}`
- 查询字段：`{queryFields}`
- 导入字段口径：`{importFields}`
- 导出字段口径：`{exportFields}`

强制要求：
1. QueryDTO 中每个查询字段，必须在 `buildQuery` 明确落条件。
2. 分页接口兼容 Spring `Pageable`。
3. 导入实现遵循：批量预取 + 前置校验 + 批量落库；禁止循环内远程调用。
4. 导入失败返回错误文件ID，并提供可下载机制（如项目已有通用下载端点则复用）。
5. 所有写操作使用 `@Transactional(rollbackFor = Exception.class)`。
6. 业务失败返回可读 `msg`，供前端直接展示。
7. 产出后给出最小测试清单（含查询条件生效验证）。
