package com.vortex.cloud.test.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目类型枚举
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProjectTypeEnum {
    /**
     * 项目
     */
    PROJECT("PROJECT", "项目"),
    /**
     * 产品
     */
    PRODUCT("PRODUCT", "产品");

    private final String key;
    private final String value;

    public static ProjectTypeEnum getByKey(String key) {
        for (ProjectTypeEnum value : values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return null;
    }

    public static ProjectTypeEnum getByValue(String text) {
        for (ProjectTypeEnum value : values()) {
            if (value.getValue().equals(text)) {
                return value;
            }
        }
        return null;
    }

    public static boolean containsKey(String key) {
        return getByKey(key) != null;
    }

    public static Set<String> keySet() {
        return Arrays.stream(values()).map(ProjectTypeEnum::getKey).collect(Collectors.toSet());
    }

    public static Set<String> valueSet() {
        return Arrays.stream(values()).map(ProjectTypeEnum::getValue).collect(Collectors.toSet());
    }
}
