package com.example.demo;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@Slf4j
// 开启定时任务支持：PhotoPullTask 拉取轮询、PhotoCleanupTask 定期清理（固定间隔）均依赖 @Scheduled
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FileApplication {

    public static void main(String[] args) {
        // 必须在 Spring 初始化日志系统之前写入 LOG_DIR 系统属性：logback-spring.xml 依赖它定位日志目录
        System.setProperty("LOG_DIR", resolveLogDir());
        SpringApplication.run(FileApplication.class, args);
    }

    /**
     * 日志目录解析规则（优先级从高到低）：
     * <ol>
     * <li>显式指定：系统属性 LOG_DIR（java -DLOG_DIR=... -jar file.jar）或环境变量 FILE_RECEIVER_LOG_DIR；</li>
     * <li>默认：软件目录（file.jar 所在目录）下的 Logs 子目录。</li>
     * </ol>
     * IDE / 裸 class 运行时 ApplicationHome 退化为 classpath 根目录，Logs 落在该目录下，仅影响开发场景。
     */
    private static String resolveLogDir() {
        String explicit = System.getProperty("LOG_DIR", System.getenv("FILE_RECEIVER_LOG_DIR"));
        if (StrUtil.isNotBlank(explicit)) {
            return explicit;
        }
        File softwareDir = new ApplicationHome(FileApplication.class).getDir();
        return new File(softwareDir, "Logs").getAbsolutePath();
    }
}
