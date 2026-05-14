package com.vortex.cloud.test.service;

import cn.hutool.core.date.DateRange;
import com.alibaba.druid.DbType;
import com.vortex.cloud.vfs.lite.data.dto.ColumnDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.dto.IndexDefinitionDTO;
import com.vortex.cloud.vfs.lite.data.dto.TableDefinitionDTO;

import java.util.Date;
import java.util.List;

/**
 * @author donghao
 * @date 2025/4/23 11:44
 */
public interface TableService {

    /**
     * 按月分表的表定义
     * @return
     */
    List<TableDefinitionDTO> getMonthSubTableDefinition();

    /**
     * 执行按月分表逻辑
     */
    void doMonthSubTable();

    /**
     * 为指定表新增字段与索引
     * @param columnDefinitions
     * @param indexDefinitions
     * @param dateRange
     */
    void doAlterTable(List<ColumnDefinitionDTO> columnDefinitions, List<IndexDefinitionDTO> indexDefinitions, DateRange dateRange, Date startMonth );

    /**
     * 获取数据库类型
     * @return
     */
    DbType getDbType();
}
