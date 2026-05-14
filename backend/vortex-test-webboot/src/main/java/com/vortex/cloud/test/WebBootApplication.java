package com.vortex.cloud.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zhanglei
 */
@Slf4j
@EnableCaching
@EnableScheduling
@EnableDiscoveryClient
@ServletComponentScan(basePackages = "com.vortex")
@SpringBootApplication(scanBasePackages = "com.vortex")
public class WebBootApplication {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(WebBootApplication.class, args);
        String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
        for (String profile : activeProfiles) {
            log.error("Spring Boot 使用profile为:{}", profile);
        }
    }
}
