# 后端编码规范（backend 生效）

本文件仅约束 `backend` 目录下代码生成、改造与评审。

## A. 精简版（硬规则）

### A1. 循环体性能约束（强制）
1. 默认禁止在循环体内执行 SQL 查询、Mapper 查询、远程接口调用。
2. 必须优先采用“批量预取 + 内存映射（Map）”模式：循环外一次性查询/拉取，循环内仅做内存匹配与组装。
3. 若确实无法批量化，才允许逐条查询/调用；代码中必须用注释说明不可批量化原因与影响评估。
4. 本约束适用于导入、列表处理、同步任务、批量校验、批量落库等所有批处理场景。

### A2. 远程/跨模块调用防御式编程（强制）
1. 远程/跨模块返回值一律视为不可信（可能 null / 空集合）。
2. 禁止链式调用远程结果（如 `xxxService.xxx().stream()`、`...getField()`）。
3. 远程调用结果必须先落地局部变量并做判空/判空集合。
4. 禁止裸 NPE；校验失败按业务语义处理（返回空、降级或抛业务异常）。

### A3. 导入导出设计规则（强制）
1. 导入模板禁止直接暴露内部主键（如 `id`、`*Id`）。
2. 关联字段统一使用业务键（如工号、编码、名称、外部标识）作为导入依据。
3. 系统必须在导入过程中完成“业务键 -> 内部ID”自动反查与回填。
4. 任何关联实体（人员、部门、用户、组织、字典、业务主数据等）均禁止要求客户填写内部ID。
5. 反查失败必须收集到导入错误信息（`messages`）并生成错误文件返回，不得在落库阶段抛运行时异常给客户。
6. 导入校验必须前置到 `buildExcelFields`（含 `convertFunction` / `rowValidateFunction`）阶段；`saveOrUpdateList` 仅处理已通过预校验的数据。
7. 导入日期字段必须同时识别业务约定字符串（如 `yyyy-MM-dd`）与 Excel 日期序列号字符串（如 `45110` / `45110.0`）。Excel 单元格显示为 `yyyy-mm-dd` 时，底层值仍可能被读取为序列号；该兼容逻辑必须配中文注释说明来源，禁止无依据堆叠大量未验证格式分支。
8. 导入小数字段必须区分“真实超精度”和“Excel/Double 浮点尾差”。读取为数值类型时可设置极小容忍阈值（如 `0.000000001`）识别 `84.65` 被读成 `84.65000000000001` 的情况；但 `1.2345` 这类真实超过业务小数位的值仍必须失败。该兼容逻辑必须配中文注释说明来源，禁止简单截断或无条件四舍五入放过。
9. 导入重复校验边界必须按业务需求确认清楚：仅校验 Excel 本文件、还是同时校验数据库存量，不能默认套用某个模块的规则。若需要与数据库存量比对，必须优先按本次导入涉及的业务 key 批量预取，禁止未经评估全量加载当前租户历史数据。

### A4. 事务规范（强制）
1. 所有写操作（新增/修改/删除）统一使用注解式事务 `@Transactional(rollbackFor = Exception.class)`。
2. 禁止使用 TransactionTemplate 等编程式事务替代上述默认规范（除非有明确特殊需求并说明原因）。

### A5. 数据库设计规则
1. 所有表禁止使用物理外键，不设置 `FOREIGN KEY` 约束。
2. 关联关系仅存储关联ID（如 `staffId`、`userId`、`deptId`）。
3. 允许按业务需要冗余名称/编码字段（如 `staffName`、`userName`、`deptName`）。
4. 关联数据校验、回填、一致性维护全部在业务层处理。

### A6. 唯一性规则
1. 不生成数据库唯一索引、不生成联合唯一索引。
2. 业务层必须进行唯一性校验（通常按 `tenantId + 业务字段`）。

### A7. Maven 执行规范
1. Maven 命令统一使用：`mvn -s /Users/shibin/.m2/settings.xml`。
2. 除非明确要求，不额外添加 `-Dmaven.repo.local`。

### A8. 查询与分页契约（强制）
1. `QueryDTO` 新增的每个查询字段，必须在 `buildQuery` 中显式落条件。
2. 分页接口应与 Spring `Pageable` 契约保持一致（`page` 0基、`size` 每页条数）。
3. 若调整分页/查询参数契约，必须同步通知并更新前端调用。
4. 普通列表、分页、导出查询禁止在后端静默补默认日期范围；前端查询日期被清空或请求未传日期时，后端应按“无日期过滤”处理。
5. 默认日期范围只允许用于明确的统计/看板/时间窗接口，且必须在接口契约、方法命名或注释中说明默认口径，避免与普通列表查询混用。

### A9. 导入失败可诊断性（强制）
1. 导入失败除 `msg` 外，必须返回可定位错误的标识（如错误文件ID）。
2. 项目应提供错误文件可下载机制（已有通用接口则复用），避免前端仅拿到ID无法排错。
3. 导入失败的 `msg` 应尽量包含首个错误行或错误摘要；错误文件列必须写入可读原因，禁止只返回“导入失败”但错误文件为空。
4. Controller/Service 捕获导入异常后必须记录异常日志（含堆栈和关键业务参数），再返回可读失败信息；禁止吞异常导致后台无日志、前端无定位线索。

### A10. 多模块 Maven 验收命令约定（强制）
1. 本项目为 Maven 多模块工程，新增/修改 service 层测试时必须带 `-am` 编译依赖模块，避免新加的 `domain/support/dao` 类在单模块测试中不可见。
2. 指定单个测试类并带 `-am` 时，必须追加 `-Dsurefire.failIfNoSpecifiedTests=false`，避免依赖模块因不存在该测试类而失败。
3. service 层测试推荐命令模板：`mvn -s /Users/shibin/.m2/settings.xml -pl vortex-test-service -am -Dtest=XXXTest -Dsurefire.failIfNoSpecifiedTests=false test`。
4. controller 编译验收推荐命令模板：`mvn -s /Users/shibin/.m2/settings.xml -pl vortex-test-controller -am -DskipTests compile`。
5. controller 层若已落测试类，推荐命令模板：`mvn -s /Users/shibin/.m2/settings.xml -pl vortex-test-controller -am -Dtest=XXXControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`；若未落测试类，至少执行第4条编译验收。
6. 交付报告必须记录实际执行命令、通过/失败结果；若失败属于环境或存量问题，应与本次业务代码问题分层说明。

### A11. 后端接口冒烟留痕（强制）
1. 严格交付场景下，除单元测试外，必须至少保留一条“接口冒烟”执行证据（可为 Controller 测试、MockMvc、Postman/Newman 或等效自动化命令）。
2. 接口冒烟证据必须包含：接口范围、执行命令、执行结果、关键输出摘要；仅描述“已验证”不算证据。
3. 若受环境限制无法执行接口冒烟，报告中必须明确阻塞原因、影响范围、替代验证手段与待补动作。

### A12. 后端服务生效确认（强制）
1. 需要真实接口联调时，冒烟前必须确认后端服务状态（端口/进程）并记录当前访问地址。
2. 本次改动涉及 Controller/Service/配置等运行时代码时，默认要求重启后端服务或采用等效的“已加载最新代码”确认手段；仅编译通过不等同运行时生效。
3. 若复用已有后端进程，必须补充版本/生效确认依据（如启动时间、日志标识、健康检查返回口径）；无法确认时不得标记为“接口联调已通过”。
4. 报告中必须记录：是否重启、重启命令或操作、联调所用 base URL、接口冒烟结果。
5. 本项目后端本地启动命令 `mvn -s /Users/shibin/.m2/settings.xml spring-boot:run` 必须在 `backend/vortex-test-webboot` 目录执行；禁止在上级聚合目录直接启动导致模块未按预期加载。

### A13. 小数精度规则（强制）
1. 业务小数字段默认使用 `BigDecimal`，禁止使用 `double/Double/float/Float` 承载金额、工时、人次、占比等需要精确校验的字段。
2. DTO 必须用 `@Digits(integer = x, fraction = y)` 明确整数位与小数位；实体列定义必须与 DTO 精度一致，如 `decimal(10,3)` 对应 `integer = 7, fraction = 3`。
3. 保存与导入都必须执行同一口径的小数校验；禁止前端允许、后端截断，或导入口径与页面保存口径不一致。
4. “最多 N 位小数”不是“固定 N 位小数”。后端可统一设置 scale 便于入库，但错误提示和业务语义必须表达为“最多保留 N 位小数”。
5. 禁止为了通过校验对真实超精度小数做静默截断；若确需四舍五入，必须是明确业务需求，并在代码注释中说明。

## B. 示例版（参考落地）

### B1. 反例与正例：循环内远程调用
```java
// 反例：循环内逐条远程调用（禁止）
for (Item item : items) {
    CloudUserDTO user = umsService.getUserByStaffId(tenantId, item.getStaffId());
}

// 正例：批量预取 + Map匹配（推荐）
List<SimpleStaffDTO> staffList = umsService.loadSimpleStaffs(tenantId);
Map<String, SimpleStaffDTO> staffMap = staffList.stream()
        .collect(Collectors.toMap(SimpleStaffDTO::getId, Function.identity(), (a, b) -> a));
for (Item item : items) {
    SimpleStaffDTO staff = staffMap.get(item.getStaffId());
}
```

### B2. 反例与正例：导入内部ID字段
```java
// 反例：让客户填 staffId（禁止）
fields.add(ExcelImportField.builder().key("staffId").title("人员ID").required(true).build());

// 正例：让客户填 staffName，系统反查回填 staffId（推荐）
fields.add(ExcelImportField.builder().key("staffName").title("人员名称").required(true).build());
```

### B3. 反例与正例：导入校验抛异常
```java
// 反例：落库阶段直接抛异常（禁止）
Assert.notNull(userDTO, "人员不存在");

// 正例：前置校验收集 messages（推荐）
convertFunction((messages, source) -> {
    if (!valid) {
        messages.add("人员不存在");
        return null;
    }
    return source;
});
```

### B4. 正例：导入日期兼容 Excel 序列号
```java
String dateString = source instanceof Date
        ? DateUtil.format((Date) source, DatePattern.NORM_DATE_PATTERN)
        : source.toString().trim();

// Excel 日期可能被读取为序列号字符串，例如 45110 表示 2023-07-03。
if (dateString.matches("^\\d+(\\.0+)?$")) {
    return excelSerialDateToLocalDate(Double.parseDouble(dateString))
            .format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
}
```

### B5. 正例：导入小数兼容 Excel 浮点尾差
```java
private static final BigDecimal EXCEL_NUMBER_EPSILON = new BigDecimal("0.000000001");

private BigDecimal normalizeImportDecimal(BigDecimal value, int scale) {
    if (value.stripTrailingZeros().scale() <= scale) {
        return value.setScale(scale, RoundingMode.UNNECESSARY);
    }
    BigDecimal rounded = value.setScale(scale, RoundingMode.HALF_UP);
    // Excel 数值单元格可能带二进制浮点尾差，例如 84.65 被读成 84.65000000000001；这种误差不应当按第 4 位小数拦截。
    if (value.subtract(rounded).abs().compareTo(EXCEL_NUMBER_EPSILON) <= 0) {
        return rounded;
    }
    return null;
}
```
