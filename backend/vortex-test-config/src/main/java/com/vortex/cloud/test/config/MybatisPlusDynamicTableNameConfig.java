package com.vortex.cloud.test.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.vortex.cloud.test.support.TableNameThreadLocal;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;


/**
 * @author donghao
 * @date 2025/4/21 12:25
 */
@Configuration
public class MybatisPlusDynamicTableNameConfig {

    @Resource
    private MybatisPlusInterceptor interceptor;

    @PostConstruct
    public void addInterceptor() {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            String actualTableName = TableNameThreadLocal.get();
            return StrUtil.isNotBlank(actualTableName) ? actualTableName : tableName;
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
    }
}
