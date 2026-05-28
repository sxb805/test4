# 自动生成执行证据报告（2026-05-28）

## 1. 结论总览
- 功能可用性：待人工补充（可依据下文执行记录快速判定）。
- 本次改动质量：待人工补充。
- 全局门禁：待人工补充。

## 2. 执行记录（命令 + 结果 + 关键摘要）
1. 时间：2026-05-28 10:41:00\n- 步骤：project-list\n- 结果：PASS\n- 命令：`curl -sS -X GET 'http://127.0.0.1:16666/cloud/sample/project/list' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n2. 时间：2026-05-28 10:41:00\n- 步骤：save\n- 结果：PASS\n- 命令：`curl -sS -X POST 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/save' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b' --data-binary '@/Users/shibin/code/hjy/zhsw/test4/scripts/artifacts/taskworkitem-smoke-20260528-104100/save.json'`\n3. 时间：2026-05-28 10:41:01\n- 步骤：page\n- 结果：PASS\n- 命令：`curl -sS -X GET 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/page?page=0&size=10&moduleName=%E5%86%92%E7%83%9F%E6%A8%A1%E5%9D%97-104100' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n4. 时间：2026-05-28 10:41:01\n- 步骤：get\n- 结果：PASS\n- 命令：`curl -sS -X GET 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/get?id=2059827250586247170' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n5. 时间：2026-05-28 10:41:01\n- 步骤：delete\n- 结果：PASS\n- 命令：`curl -sS -X POST 'http://127.0.0.1:16666/cloud/sample/taskWorkItem/delete?ids=2059827250586247170' -H 'Authorization: Bearer a1b2****fe6d' -H 'tenantId: b85642556e61405290817a53ee828582' -H 'userId: dd6155d504516b9ee9f76be5a95d486b'`\n
## 3. 后端接口冒烟记录
- 产物目录：
- 关键响应文件： / 

## 4. 前端关键路径冒烟记录
- 如有 Playwright 产物，请补充  关键摘要。

## 5. 变更清单与契约清单
- 待人工补充。

## 6. 失败归因分层
- 业务问题：待人工补充。
- 环境问题：待人工补充。
- 存量问题：待人工补充。

## 7. 本次测试时间与环境口径
- 时间：2026-05-28
- 产物目录：

## 附录：summary.env
```env
run_id=taskworkitem-smoke-20260528-104100
module=taskWorkItem
module_key=taskworkitem
base_api=http://127.0.0.1:16666/cloud/sample
project_id=2059542062530097153
owner_id=62f77d14531892d50fadf21b81983641
task_id=2059827250586247170
marker=冒烟模块-104100
result=PASS
```
