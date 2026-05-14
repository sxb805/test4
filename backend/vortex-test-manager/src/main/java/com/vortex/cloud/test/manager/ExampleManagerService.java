package com.vortex.cloud.test.manager;

import com.google.common.collect.Lists;
import com.vortex.cloud.test.dto.ExampleVO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhanglei
 */
@Component
public class ExampleManagerService {

    @Cacheable("EXAMPLE_CACHE")
    public List<ExampleVO> cacheExample(String tenantId) {
        return Lists.newArrayList();
    }
}
