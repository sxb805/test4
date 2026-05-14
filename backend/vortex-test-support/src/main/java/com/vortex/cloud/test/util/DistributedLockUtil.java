package com.vortex.cloud.test.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 加锁公共方法
 */
public class DistributedLockUtil {

    // 从 Spring 上下文拿，不是 static 注入
    private static final RedissonClient redissonClient = SpringContextUtil.getBean(RedissonClient.class);

    /**
     * 加锁 + 执行业务（标准用法）
     */
    public static <T> T lock(String key, long waitSeconds, java.util.function.Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        try {
            boolean success = lock.tryLock(waitSeconds, TimeUnit.SECONDS);
            if (!success) {
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("加锁中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 无返回值版本
     */
    public static void lock(String key, long waitSeconds, Runnable runnable) {
        lock(key, waitSeconds, () -> {
            runnable.run();
            return null;
        });
    }
}
