# 客户信息管理 MySQL 表结构设计

## 1. 表说明
- 表名：`customer_info`
- 说明：客户信息主表
- 存储引擎：`InnoDB`
- 字符集：`utf8mb4`

## 2. 建表 SQL

```sql
CREATE TABLE `customer_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
  `customer_code` VARCHAR(64) NOT NULL COMMENT '客户编号（唯一）',
  `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(30) NOT NULL COMMENT '联系电话',
  `industry` VARCHAR(50) NOT NULL COMMENT '所属行业',
  `customer_status` VARCHAR(20) NOT NULL COMMENT '客户状态：NORMAL/INTENTION/DISABLED',
  `region` VARCHAR(100) DEFAULT NULL COMMENT '所在地区',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0否 1是',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_industry` (`industry`),
  KEY `idx_customer_status` (`customer_status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';
```

## 3. 字段字典

| 字段名 | 类型 | 允许空 | 默认值 | 键 | 说明 |
|---|---|---|---|---|---|
| id | BIGINT UNSIGNED | 否 | AUTO_INCREMENT | PK | 主键ID |
| customer_name | VARCHAR(100) | 否 | - | IDX | 客户名称 |
| customer_code | VARCHAR(64) | 否 | - | UK | 客户编号（唯一） |
| contact_person | VARCHAR(50) | 否 | - | - | 联系人 |
| contact_phone | VARCHAR(30) | 否 | - | - | 联系电话 |
| industry | VARCHAR(50) | 否 | - | IDX | 所属行业 |
| customer_status | VARCHAR(20) | 否 | - | IDX | 客户状态 |
| region | VARCHAR(100) | 是 | NULL | - | 所在地区 |
| remark | VARCHAR(500) | 是 | NULL | - | 备注 |
| is_deleted | TINYINT(1) | 否 | 0 | - | 逻辑删除标识 |
| created_time | DATETIME | 否 | CURRENT_TIMESTAMP | IDX | 创建时间 |
| updated_time | DATETIME | 否 | CURRENT_TIMESTAMP | - | 更新时间 |

## 4. 状态枚举建议
- `NORMAL`：正常
- `INTENTION`：意向
- `DISABLED`：停用

## 5. 索引建议
- 唯一索引：`customer_code`，保障客户编号不重复。
- 普通索引：`customer_name`、`industry`、`customer_status`、`created_time`，提升筛选性能。

## 6. 版本记录
- `v1.0`（2026-05-05）：初版建立。

