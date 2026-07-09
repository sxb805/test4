package com.vortex.cloud.test.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务工时所属公司枚举
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TaskWorkItemCompanyEnum {
    /**
     * 苏州伏泰
     */
    SUZHOU_FUTAI("SUZHOU_FUTAI", "苏州伏泰"),
    /**
     * 苏州环境云
     */
    SUZHOU_ENV_CLOUD("SUZHOU_ENV_CLOUD", "苏州环境云");

    private final String key;
    private final String value;

    public static TaskWorkItemCompanyEnum getByKey(String key) {
        for (TaskWorkItemCompanyEnum value : values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return null;
    }

    public static TaskWorkItemCompanyEnum getByValue(String text) {
        for (TaskWorkItemCompanyEnum value : values()) {
            if (value.getValue().equals(text)) {
                return value;
            }
        }
        return null;
    }

    public static boolean containsKey(String key) {
        return getByKey(key) != null;
    }

    public static Set<String> valueSet() {
        return Arrays.stream(values()).map(TaskWorkItemCompanyEnum::getValue).collect(Collectors.toSet());
    }
}
