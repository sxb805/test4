# 导入模板相对路径修复执行证据报告

## 1. 结论总览
- 功能可用性：通过。导入模板下载地址已从根路径 `/resources/template/...` 改为相对路径 `./resources/template/...`，适配线上子路径前缀发布。
- 本次改动质量：通过。已执行相关页面局部 eslint，并确认无绝对模板路径残留。
- 全局门禁：未执行全量构建；本次为静态资源 URL 小范围修复。

## 2. 执行记录
| 序号 | 命令 | 结果 | 关键摘要 |
| --- | --- | --- | --- |
| 1 | `rg -n "importTemplateURL: \\"/resources/template|templateURL: \\"/resources/template|/resources/template/.*导入模板" frontend/src frontend/config -S` | 通过 | 定位到 `project`、`taskWorkItem`、`example`、`demo` 四处模板绝对路径。 |
| 2 | `rg -n "publicPath|base:|history|hash|resources/template" frontend/config frontend/src -S` | 通过 | 生产配置 `publicPath: './'`，模板路径应相对化以适配发布前缀。 |
| 3 | `pnpm -s eslint src/pages/project/index.js src/pages/taskWorkItem/index.js src/pages/example/index.js src/pages/demo/index.js --ext .js`（目录：`frontend`） | 通过 | 仅输出存量配置告警：`React version not specified in eslint-plugin-react settings`。 |
| 4 | `rg -n "importTemplateURL: \\"/resources/template|templateURL: \\"/resources/template" frontend/src frontend/config -S` | 通过 | 命令退出码 1，无匹配结果，确认模板绝对路径已清理。 |

## 3. 后端接口冒烟记录
- 本次未修改后端代码。
- 不涉及后端接口冒烟。

## 4. 前端关键路径冒烟记录
- 已执行相关页面局部 eslint。
- 未启动前端服务做点击下载冒烟；本次变更为静态模板 URL 字符串调整，已通过代码检索确认。

## 5. 变更清单与契约清单
- `frontend/src/pages/project/index.js`：`./resources/template/项目导入模板.xlsx`
- `frontend/src/pages/taskWorkItem/index.js`：`./resources/template/任务工单管理导入模板.xlsx`
- `frontend/src/pages/example/index.js`：`./resources/template/样例导入模板.xlsx`
- `frontend/src/pages/demo/index.js`：`./resources/template/企业用户导入模板.xlsx`
- 导入接口与错误文件下载接口未变。

## 6. 风险与遗留项
- 业务问题：无已知阻塞。
- 环境问题：未做线上前缀环境实测。
- 存量问题：前端 eslint 存在 React 版本配置 warning，非本次引入。
- 本次测试时间与环境：2026-06-09 18:56:13 CST；工作目录 `/Users/shibin/code/hjy/zhsw/test4`；前端命令目录 `/Users/shibin/code/hjy/zhsw/test4/frontend`。
