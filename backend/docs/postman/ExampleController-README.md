# ExampleController Postman 使用说明

## 1. 文件位置
- Collection: `docs/postman/ExampleController.postman_collection.json`
- Environment: `docs/postman/ExampleController.postman_environment.json`

## 2. 导入 Postman
1. 打开 Postman -> Import
2. 选择上面两个 JSON 文件导入
3. 切换环境为 `ExampleController Local`

## 3. 默认可运行参数
- `baseUrl`: `http://127.0.0.1:16666/cloud/sample`
- `tenantId`: `T1001`
- `userId`: `U1001`
- `exampleId`: `EXAMPLE-1001`（会被 `1. 分页 page` 自动覆盖为真实第一条 ID）

## 4. 推荐执行顺序
1. `1. 分页 page`（自动提取 `exampleId`）
2. `6. 查看 get`
3. `3. 保存 save`
4. `4. 修改 update`
5. `7. 校验 exist`
6. `5. 删除 delete`
7. `8. 导入 importExcel`
8. `9. 导出 exportExcel`

## 5. 注意事项
- `save/update` 必填重点：`tenantId`(Header), `code`, `name`；`update` 还需要 `id`。
- `importExcel` 需要把 `file` 替换为本地真实 Excel 文件。
- 若后端不是本机默认端口，请修改环境变量 `baseUrl`。
