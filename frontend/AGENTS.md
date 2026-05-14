# 前端项目规范（CRUD 生成专用）

本文件仅约束 `frontend` 代码生成与改造；后端规范以 `backend/AGENTS.md` 为准。

## 1. 接口与路径硬约束
1. 后端接口统一使用前缀：`/cloud/sample`。
2. 页面 service 必须声明 `API_PREFIX`，禁止在页面中散落硬编码接口。
3. CRUD 基础接口按资源统一：
   - `page`: `POST ${API_PREFIX}/{resource}/page`
   - `get`: `GET ${API_PREFIX}/{resource}/get`
   - `save`: `POST ${API_PREFIX}/{resource}/save`
   - `update`: `POST ${API_PREFIX}/{resource}/update`
   - `delete`: `POST ${API_PREFIX}/{resource}/delete`
4. 导入导出接口默认：
   - `import`: `POST ${API_PREFIX}/{resource}/importExcel`
   - `export`: `POST ${API_PREFIX}/{resource}/exportExcel`

## 2. 页面生成标准结构
1. 每个 CRUD 页面固定目录：
   - `src/pages/{resource}/index.js`
   - `src/pages/{resource}/model.js`
   - `src/pages/{resource}/service.js`
   - `src/pages/{resource}/components/Add.js`
   - `src/pages/{resource}/components/View.js`
   - `src/pages/{resource}/components/Export.js`
2. 必须同步注册路由 `config/routes.js`，禁止只建页面不挂路由。
3. `namespace` 必须与页面资源名一致，禁止沿用 demo 的 `user` 等历史命名。

## 3. 日期与表单稳定性约束
1. `DatePicker` 字段回填必须先转 `dayjs`；禁止直接把接口字符串灌入 `DatePicker`。
2. `Form` 禁止使用包含日期字符串的 `initialValues` 直接初始化。
3. 提交保存前必须把日期字段转换为后端约定字符串：
   - 日期：`YYYY-MM-DD`
   - 时间：`YYYY-MM-DD HH:mm:ss`

## 4. 错误提示与交互约束
1. 保存/更新接口返回失败时，必须优先弹后端 `msg`：`message.error(res.msg || "操作失败")`。
2. 删除、导入、导出失败都必须给用户可见提示，禁止静默失败。
3. 页面禁止“只判断布尔成功”；需要兼容后端 `{ result, msg, data }` 返回结构。

## 5. 组件选型约束
1. 优先使用项目中已稳定使用的组件组合（`antd + @vtx/components` 已有用法）。
2. 新组件接入前，先在仓库中检索是否已有同类使用示例；无示例时优先保守实现。

## 6. CRUD 冒烟验收清单（提交前必过）
1. 列表可加载，分页正常。
2. 新增：成功提示正确；失败可见后端 `msg`。
3. 编辑：打开不崩溃，保存后页面不白屏，列表可刷新。
4. 查看：弹窗可打开，字段显示正常。
5. 删除：可成功删除并刷新。
6. 导入：成功与失败路径都能提示，失败可下载错误文件（若后端提供）。
7. 导出：全部/选中/当前页均可下载。

## 7. 生成提示词最小必填信息（给 Agent/Skill）
1. 资源名（如 `example`）、中文标题（如“样例”）。
2. 后端 context-path（本项目固定 `/cloud/sample`）。
3. DTO/VO/Query 字段清单（含日期字段类型）。
4. 导入导出接口与模板地址。
5. 成功/失败消息策略（失败透出后端 `msg`）。
