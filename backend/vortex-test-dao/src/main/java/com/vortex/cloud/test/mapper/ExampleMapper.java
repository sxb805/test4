package com.vortex.cloud.test.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.cloud.test.domain.Example;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhanglei
 */
@Mapper
public interface ExampleMapper extends BaseMapper<Example> {
}
