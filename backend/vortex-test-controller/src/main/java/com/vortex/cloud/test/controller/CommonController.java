package com.vortex.cloud.test.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import com.google.common.collect.Maps;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author zhanglei
 */
@Tag(name = "通用接口")
@RestController
@RequestMapping("common")
public class CommonController {
    private static final Map<String, Class<?>> ENUM_CLASS_MAP = CollUtil.toMap(ClassUtil.scanPackage("com.vortex.cloud.test.enums"), Maps.newHashMap(), Class::getSimpleName);

    @Operation(summary = "枚举")
    @RequestMapping(value = "loadEnum", method = {RequestMethod.GET, RequestMethod.POST})
    public RestResultDTO<Object[]> list(
            @Parameter(description = "枚举类名", examples = {
                    @ExampleObject(name = "样例枚举", value = "ExampleEnum")
            }) @RequestParam String enumClassName) {
        Class<?> enumClass = ENUM_CLASS_MAP.get(enumClassName);
        Assert.notNull(enumClass, "找不到名为" + enumClassName + "的枚举");
        return RestResultDTO.newSuccess(enumClass.getEnumConstants());
    }
}
