package com.vortex.cloud.test.scheduler;

import com.vortex.cloud.test.service.TableService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhanglei
 */
@Component
public class TableTask {

    @Resource
    private TableService tableService;

    /**
     * 分表DDL维护
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void splitTable() {
        tableService.doMonthSubTable();
    }

}
