# 客户信息管理接口清单（前后端标准）

## 1. 接口总览
- 新增客户
- 编辑客户
- 删除客户（单条）
- 批量删除客户
- 客户分页列表
- 客户详情
- 客户统计（用于顶部卡片）

## 2. 通用约定
- 基础路径：`/api/customers`
- 数据格式：`application/json`
- 时间字段格式：`yyyy-MM-dd HH:mm:ss`
- 客户状态枚举：
  - `NORMAL`
  - `INTENTION`
  - `DISABLED`

## 3. 接口详情

### 3.1 新增客户
- 方法：`POST`
- 路径：`/api/customers`

请求体示例：
```json
{
  "customerName": "上海星辰科技有限公司",
  "customerCode": "CUST20260001",
  "contactPerson": "张三",
  "contactPhone": "13800138000",
  "industry": "互联网",
  "customerStatus": "NORMAL",
  "region": "上海-浦东新区",
  "remark": "重点跟进客户"
}
```

### 3.2 编辑客户
- 方法：`PUT`
- 路径：`/api/customers/{id}`

请求体示例：
```json
{
  "customerName": "上海星辰科技有限公司",
  "customerCode": "CUST20260001",
  "contactPerson": "李四",
  "contactPhone": "13800138001",
  "industry": "互联网",
  "customerStatus": "INTENTION",
  "region": "上海-徐汇区",
  "remark": "已安排二次沟通"
}
```

### 3.3 删除客户（单条）
- 方法：`DELETE`
- 路径：`/api/customers/{id}`

### 3.4 批量删除客户
- 方法：`DELETE`
- 路径：`/api/customers/batch`

请求体示例：
```json
{
  "ids": [101, 102, 103]
}
```

响应示例：
```json
{
  "successCount": 3,
  "failCount": 0
}
```

### 3.5 客户分页列表
- 方法：`GET`
- 路径：`/api/customers`

Query 参数：
- `pageNum`（页码）
- `pageSize`（页大小）
- `customerName`（客户名称模糊匹配）
- `industry`（所属行业）
- `customerStatus`（客户状态）
- `createdTimeStart`（创建开始时间）
- `createdTimeEnd`（创建结束时间）

响应示例：
```json
{
  "total": 120,
  "pageNum": 1,
  "pageSize": 10,
  "list": [
    {
      "id": 1,
      "customerName": "上海星辰科技有限公司",
      "customerCode": "CUST20260001",
      "contactPerson": "张三",
      "contactPhone": "13800138000",
      "industry": "互联网",
      "customerStatus": "NORMAL",
      "region": "上海-浦东新区",
      "remark": "重点跟进客户",
      "createdTime": "2026-05-05 10:00:00",
      "updatedTime": "2026-05-05 10:00:00"
    }
  ]
}
```

### 3.6 客户详情
- 方法：`GET`
- 路径：`/api/customers/{id}`

### 3.7 客户统计（顶部 4 卡片）
- 方法：`GET`
- 路径：`/api/customers/statistics`

响应示例：
```json
{
  "totalCount": 120,
  "normalCount": 80,
  "intentionCount": 30,
  "disabledCount": 10
}
```

## 4. 字段校验建议
- `customerName`：必填，2~100 字符。
- `customerCode`：必填，建议 4~64 字符，且唯一。
- `contactPerson`：必填，1~50 字符。
- `contactPhone`：必填，需符合手机号/固话格式。
- `industry`：必填。
- `customerStatus`：必填，且必须在枚举值内。

## 5. 错误码建议
- `400`：参数错误
- `404`：客户不存在
- `409`：客户编号重复
- `500`：系统异常

## 6. 版本记录
- `v1.0`（2026-05-05）：初版建立。

