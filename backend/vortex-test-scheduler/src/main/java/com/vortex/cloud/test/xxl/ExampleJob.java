package com.vortex.cloud.test.xxl;

import com.vortex.cloud.test.support.Constants;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.ShardingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExampleJob {
    @XxlJob(value = "example-job",
            jobCron = "0 0/5 * * * ?",
            jobDesc = "示例定时任务",
            author = "xxl",
            executorRouteStrategy = Constants.EXECUTOR_ROUTE_STRATEGY,
            executorBlockStrategy = Constants.EXECUTOR_BLOCK_STRATEGY,
            triggerStatus = "1",
            executorParam = "{\"startTime\":null,\"endTime\":null}")
    public ReturnT<String> test(String param) {
        long start = System.currentTimeMillis();
        log.error("ExampleJob定时任务开始");
        try {
            // 分片处理
            ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
            Integer shardIndex = shardingVO.getIndex();
            Integer shardTotal = shardingVO.getTotal();
            // TODO
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            log.error("ExampleJob定时任务失败", e);
        }
        long end = System.currentTimeMillis();
        log.error("ExampleJob定时任务结束，耗时：{}ms", end - start);
        return ReturnT.SUCCESS;
    }
}
