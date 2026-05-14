package com.vortex.cloud.test.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.cloud.test.domain.HealthScoreMonthRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author donghao
 * @date 2025/4/22 10:55
 */
@Mapper
public interface HealthScoreMonthRecordMapper extends BaseMapper<HealthScoreMonthRecord> {

    @Delete({"<script>",
            "DELETE FROM ${tableName}",
            "<if test='ew != null'>",
            "${ew.customSqlSegment}",
            "</if>",
            "</script>"})
    int physicalDelete(@Param("tableName") String tableName, @Param("ew") QueryWrapper<HealthScoreMonthRecord> queryWrapper);

    @Select({"<script>",
            "SELECT health_level_name,ROUND(SUM(line_length),2) AS lineLength FROM ${tableName}",
            "<if test='ew != null'>",
            "${ew.customSqlSegment}",
            "</if> and line_length is not null  GROUP BY health_level_name",
            "</script>"})
    List<HealthScoreMonthRecord> groupByHealthLevelName(@Param("tableName") String tableName, @Param("ew") QueryWrapper<HealthScoreMonthRecord> queryWrapper);

}
