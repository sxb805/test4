package com.vortex.cloud.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.cloud.test.domain.WorkPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作计划Mapper
 */
@Mapper
public interface WorkPlanMapper extends BaseMapper<WorkPlan> {
}
