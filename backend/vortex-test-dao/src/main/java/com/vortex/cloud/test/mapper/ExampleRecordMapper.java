package com.vortex.cloud.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.cloud.test.domain.ExampleRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 样例记录Mapper
 */
@Mapper
public interface ExampleRecordMapper  extends BaseMapper<ExampleRecord> {
}
