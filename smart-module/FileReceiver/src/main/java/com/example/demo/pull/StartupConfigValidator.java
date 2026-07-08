package com.example.demo.pull;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动期配置自检与摘要日志。
 * <p>
 * 快速失败：发现配置问题时启动即抛异常终止，且**一次性报清全部问题**——避免部署后任务
 * 静默空转/起不来、现场修一轮重启一轮地逐个试错。覆盖两类问题：
 * <ol>
 * <li>拉取已启用但缺关键配置（server-url / app-id / app-secret）；</li>
 * <li>两个调度间隔（pull / cleanup 的 interval-seconds）配成 0 或负数——@Scheduled 任务不论
 * pull.enabled 与否都会注册，非正间隔会在注册阶段抛不含配置键名的异常，必须在此拦下点名。</li>
 * </ol>
 * 拉取未启用时打 WARN 提示但不阻断启动（兼容仅使用旧推送接口的过渡期部署）。
 * <p>
 * app-secret 属于密钥，摘要中永不打印内容，只标注是否已配置。
 */
@Slf4j
@Component
public class StartupConfigValidator {

    private final PhotoPullProperties properties;

    @Autowired
    public StartupConfigValidator(PhotoPullProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void validateAndReport() {
        PhotoPullProperties.Pull pull = properties.getPull();
        // 全部配置问题先收集后一次抛出：现场（远程 Windows）重启一轮成本高，禁止逐个试错
        List<String> problems = new ArrayList<>();

        // 两个调度间隔必须为正数：0/负数会漏到 @Scheduled 注册阶段抛不含配置键名的
        // IllegalArgumentException（整个程序起不来且现场无从排查）。@Scheduled 不论
        // pull.enabled 与否都会注册任务，故两者均无条件校验。间隔不承担开关语义：
        // 停用拉取走 pull.enabled=false，关闭清理走 cleanup.retention-days=0。
        if (pull.getIntervalSeconds() <= 0) {
            problems.add("file-receiver.pull.interval-seconds 必须为正数（秒），当前值：" + pull.getIntervalSeconds()
                    + "，如需停用拉取请改设 file-receiver.pull.enabled=false");
        }
        int cleanupIntervalSeconds = properties.getCleanup().getIntervalSeconds();
        if (cleanupIntervalSeconds <= 0) {
            problems.add("file-receiver.cleanup.interval-seconds 必须为正数（秒），当前值：" + cleanupIntervalSeconds
                    + "，如需关闭清理任务请改设 file-receiver.cleanup.retention-days=0");
        }

        // 拉取已启用时三项关键配置必填
        if (pull.isEnabled()) {
            if (StrUtil.isBlank(pull.getServerUrl())) {
                problems.add("缺少必需配置 file-receiver.pull.server-url");
            }
            if (StrUtil.isBlank(pull.getAppId())) {
                problems.add("缺少必需配置 file-receiver.pull.app-id");
            }
            if (StrUtil.isBlank(pull.getAppSecret())) {
                problems.add("缺少必需配置 file-receiver.pull.app-secret");
            }
        }

        if (!problems.isEmpty()) {
            // 快速失败：终止启动并一次报清全部问题
            throw new IllegalStateException("启动配置自检未通过：" + problems + "；请修正后重启");
        }

        if (!pull.isEnabled()) {
            log.warn("拉取模式未启用（file-receiver.pull.enabled=false），本程序不会主动下载任何照片；"
                    + "如需启用拉取，请配置 file-receiver.pull.* 后重启");
        } else {
            log.info("拉取模式已启用：server-url={}，app-id={}，轮询间隔={}s，app-secret=已配置（不打印）",
                    pull.getServerUrl(), pull.getAppId(), pull.getIntervalSeconds());
        }
        reportCommonConfig();
    }

    private void reportCommonConfig() {
        // 首跑延迟直接从 PhotoCleanupTask 的常量换算，避免日志文案与真实调度值漂移
        log.info("照片目录 photo-dir={}，过期清理 retention-days={}（0 表示关闭）、清理间隔={}s（启动 {}s 后首跑），日志目录={}",
                properties.getPhotoDir(), properties.getCleanup().getRetentionDays(),
                properties.getCleanup().getIntervalSeconds(),
                PhotoCleanupTask.STARTUP_DELAY_MILLIS / 1000L,
                System.getProperty("LOG_DIR", "Logs（相对进程工作目录）"));
    }
}
