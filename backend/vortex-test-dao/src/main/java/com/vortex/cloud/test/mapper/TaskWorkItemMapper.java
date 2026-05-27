package com.vortex.cloud.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.cloud.test.domain.TaskWorkItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务工单Mapper
 */
@Mapper
public interface TaskWorkItemMapper extends BaseMapper<TaskWorkItem> {
}
