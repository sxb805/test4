package com.vortex.cloud.test.service.impl;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.date.*;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vortex.cloud.test.domain.ExampleRecord;
import com.vortex.cloud.test.domain.HealthScoreMonthRecord;
import com.vortex.cloud.test.service.TableService;
import com.vortex.cloud.vfs.lite.data.dto.ColumnDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.dto.IndexDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.dto.TableDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.util.TableUtils;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author donghao
 * @date 2025/4/23 12:28
 */
@Slf4j
@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private DataSource dataSource;

    @Override
    public DbType getDbType() {
        try (Connection connection = dataSource.getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (StrUtil.equalsIgnoreCase(databaseProductName, "MySQL")) {
                return DbType.mysql;
            } else if (StrUtil.equalsIgnoreCase(databaseProductName, "DM DBMS")) {
                return DbType.dm;
            } else if (StrUtil.equalsIgnoreCase(databaseProductName, "KingbaseES")) {
                return DbType.kingbase;
            } else {
                throw new RuntimeException("暂时还不支持数据库类型" + databaseProductName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init() {
        List<TableDefinitionDTO> tableDefinitions = Lists.newArrayList();
        tableDefinitions.addAll(this.getMonthSubTableDefinition());

        TableUtils.createTable(dataSource, tableDefinitions);
        TableUtils.alterTable(dataSource, tableDefinitions);
    }

    @Override
    public List<TableDefinitionDTO> getMonthSubTableDefinition() {
        DateTime nextMonth = DateUtil.nextMonth();

        List<TableDefinitionDTO> tableDefinitions = Lists.newArrayList();
        tableDefinitions.add(
                TableDefinitionDTO.builder()
                        .tableName(HealthScoreMonthRecord.TABLE_NAME)
                        .tableComment("管网健康度评分月度数据")
                        .tableNameDateFormat(DatePattern.SIMPLE_MONTH_PATTERN)
                        .tableNameDateRange(DateUtil.range(DateUtil.parse("2025-01-01"), nextMonth, DateField.MONTH))
                        .tableNameTemplate("{tableName}_{dateString}")
                        .columnDefinitions(this.listColumnDefinition(HealthScoreMonthRecord.class))
                        .indexDefinitions(HealthScoreMonthRecord.indexDefinitions())
                        .build()
        );
        tableDefinitions.add(
                TableDefinitionDTO.builder()
                        .tableName(ExampleRecord.TABLE_NAME)
                        .tableComment("样例记录")
                        .tableNameDateFormat(DatePattern.SIMPLE_MONTH_PATTERN)
                        .tableNameDateRange(DateUtil.range(DateUtil.parse("2025-01-01"), nextMonth, DateField.MONTH))
                        .tableNameTemplate("{tableName}_{dateString}")
                        .columnDefinitions(this.listColumnDefinition(ExampleRecord.class))
                        .indexDefinitions(ExampleRecord.indexDefinitions())
                        .build()
        );
        return tableDefinitions;
    }

    @Override
    public void doMonthSubTable() {
        List<TableDefinitionDTO> tableDefinitions = this.getMonthSubTableDefinition();
        TableUtils.createTable(dataSource, tableDefinitions);
        TableUtils.alterTable(dataSource, tableDefinitions);
    }

    @Override
    public void doAlterTable(List<ColumnDefinitionDTO> columnDefinitions, List<IndexDefinitionDTO> indexDefinitions, DateRange dateRange, Date startMonth) {
        String tableNameTemplate = "{tableName}_{dateString}";
        try(Connection connection = dataSource.getConnection()) {
            String dateString = DateUtil.format(startMonth, DatePattern.SIMPLE_MONTH_PATTERN);
            Map<String, String> map = Maps.newHashMap();
            map.put("tableName", HealthScoreMonthRecord.TABLE_NAME);
            map.put("dateString", dateString);
            String tableNameWithDate = StrUtil.format(tableNameTemplate, map);
            ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableNameWithDate, (String) null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                columnDefinitions.removeIf(columnDefinition ->
                        columnDefinition != null && Objects.equals(columnName, columnDefinition.getColumnName())
                );
                indexDefinitions.removeIf(indexDefinitionDTO ->
                        indexDefinitionDTO.getColumnNames().contains(columnName));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (CollectionUtils.isEmpty(columnDefinitions)) {
            return;
        }
        List<TableDefinitionDTO> tableDefinitions = Lists.newArrayList();
        tableDefinitions.add(
                TableDefinitionDTO.builder()
                        .tableName(HealthScoreMonthRecord.TABLE_NAME)
                        .tableComment("管网健康度评分月度数据")
                        .tableNameDateFormat(DatePattern.SIMPLE_MONTH_PATTERN)
                        .tableNameDateRange(dateRange)
                        .tableNameTemplate(tableNameTemplate)
                        .columnDefinitions(columnDefinitions)
                        .indexDefinitions(indexDefinitions)
                        .build()
        );
        TableUtils.alterTable(dataSource, tableDefinitions);
    }

    private List<ColumnDefinitionDTO> listColumnDefinition(Class<?> beanClass) {
        Assert.notNull(beanClass, "entity类不能为空");
        List<ColumnDefinitionDTO> columnDefinitions = Lists.newArrayList();

        List<Class<?>> allTypes = Lists.newArrayList();
        Class<?> searchType = beanClass;
        while (searchType != null) {
            allTypes.add(searchType);
            searchType = searchType.getSuperclass();
        }

        for (int i = allTypes.size() - 1; i >= 0; i--) {
            Field[] fields = allTypes.get(i).getDeclaredFields();
            if (ArrayUtil.isEmpty(fields)) {
                continue;
            }

            for (Field field : fields) {
                Column column = AnnotationUtil.getAnnotation(field, Column.class);
                if (Objects.isNull(column)) {
                    continue;
                }

                String columnName = column.name();
                String columnDefinition = column.columnDefinition();
                if (StrUtil.isBlank(columnName)) {
                    columnName = field.getName();
                    // 转下划线
                    columnName = StrUtil.toUnderlineCase(columnName);
                }
                if (StrUtil.equalsIgnoreCase("id", columnName)) {
                    columnDefinition = "varchar(100) not null";
                }
                columnDefinitions.add(ColumnDefinitionDTO.builder().columnName(columnName).columnDefinition(columnDefinition).build());
            }
        }

        return columnDefinitions;
    }

}
