package com.vortex.cloud.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.cloud.test.domain.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
