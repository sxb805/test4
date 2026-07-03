# 任务工时管理导入重复规则调整执行证据报告

## 1. 结论总览
- 功能可用性：已去掉任务工时导入与数据库历史任务的 7 字段对比，不再命中旧数据后覆盖更新。
- 本次改动质量：导入仅校验同一个 Excel 文件内部是否重复；重复规则为项目编号、模块、开始日期、结束日期、责任人、任务描述、预计工时。
- 全局门禁：后端编译通过；未执行真实接口联调。

## 2. 执行记录
| 命令 | 结果 | 关键输出摘要 |
| --- | --- | --- |
| `mvn -s /Users/shibin/.m2/settings.xml -pl vortex-test-controller -am -DskipTests compile` | 通过 | `BUILD SUCCESS`，vortex-test-service 与 vortex-test-controller 均 `SUCCESS` |
| `rg -n "existMap\|saveOrUpdateList\|saveOrUpdateBatch\|buildUniqueKey\\(\|validateImportDuplicateRows\|saveImportList\|与第" backend/vortex-test-service/src/main/java/com/vortex/cloud/test/service/impl/TaskWorkItemServiceImpl.java` | 通过 | 仅剩 `validateImportDuplicateRows`、`saveImportList`、`buildUniqueKey` 等新逻辑，旧覆盖更新逻辑无残留 |

## 3. 后端接口冒烟记录
- 本次未执行真实接口冒烟。
- 阻塞原因：未提供可用账号密码，无法按项目规范通过 `POST /cas/login` 获取 token 后执行鉴权接口联调。
- 替代验证：执行 controller 聚合编译，覆盖 support/domain/dao/manager/service/controller 编译链路。
- 服务生效确认：未重启后端服务；本次仅完成代码编译验证，运行中服务如需联调需重启或确认已加载最新代码。

## 4. 变更清单与契约清单
- 删除导入前按租户全量查询任务并构造 `existMap` 的逻辑，避免大量历史数据进入内存。
- 导入保存从 `saveOrUpdateBatch` 调整为 `saveBatch`，导入命中数据库已有数据时不再覆盖更新。
- 新增同一 Excel 文件内部重复校验，重复时在“项目名称”列写入错误信息：`与第X行重复`。
- 新增/编辑仍不做数据库唯一性对比；导入也不做数据库唯一性对比，二者对历史数据的处理口径一致。

## 5. 风险与遗留项
- 如果数据库历史中已有重复记录，本次导入不会主动识别或修复。
- 如果用户导入的数据与数据库历史数据重复，会新增一条记录；这与当前新增/编辑不做数据库重复校验的口径一致。
- Maven 输出存在存量 warning，如 compiler plugin version 缺失等，本次未处理。

## 6. 测试时间与环境口径
- 测试时间：2026-07-03 20:06:01 CST
- 后端验证目录：`/Users/shibin/code/hjy/zhsw/test4/backend`
- 服务端口：未启动服务，未占用端口验证
