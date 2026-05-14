package com.vortex.cloud.test.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhanglei
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExampleEnum {
    /**
     * 1
     */
    YEAR("YEAR", "年"),
    /**
     * 2
     */
    MONTH("MONTH", "月"),
    /**
     * 3
     */
    DAY("DAY", "日");

    private final String key;
    private final String value;

    public static ExampleEnum getByKey(String key) {
        for (ExampleEnum value : values()) {
            if (value.getKey().equalsIgnoreCase(key)) {
                return value;
            }
        }
        return null;
    }
}
