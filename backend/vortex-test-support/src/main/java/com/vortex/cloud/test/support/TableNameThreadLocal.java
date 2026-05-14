package com.vortex.cloud.test.support;

/**
 * @author donghao
 * @date 2025/4/21 14:35
 */
public class TableNameThreadLocal {

    /**
     * 动态表名存取
     */
    private static final ThreadLocal<String> TABLE_NAME = new ThreadLocal<>();

    public static void set(String tableName) {
        TABLE_NAME.set(tableName);
    }

    public static String get() {
        return TABLE_NAME.get();
    }

    public static void remove(){
        TABLE_NAME.remove();
    }

}
