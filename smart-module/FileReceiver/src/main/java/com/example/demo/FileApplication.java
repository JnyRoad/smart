package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
// 开启定时任务支持：PhotoPullTask 拉取轮询、PhotoCleanupTask 每日清理均依赖 @Scheduled
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FileApplication {


    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }

}
