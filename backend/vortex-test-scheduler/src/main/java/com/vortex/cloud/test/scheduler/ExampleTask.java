package com.vortex.cloud.test.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ExampleTask {
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void test() {
        long start = System.currentTimeMillis();
        log.error("ExampleTask定时任务开始");
        try {
            // TODO
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            log.error("ExampleTask定时任务失败", e);
        }
        long end = System.currentTimeMillis();
        log.error("ExampleTask定时任务结束，耗时：{}ms", end - start);
    }
}
