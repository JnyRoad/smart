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
 * 快速失败：拉取模式已启用但缺关键配置（server-url / app-id / app-secret）时，
 * 启动即抛异常终止，报错一次性点名全部缺失的配置键——避免部署后任务静默空转、现场无从排查。
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
        if (!pull.isEnabled()) {
            log.warn("拉取模式未启用（file-receiver.pull.enabled=false），本程序不会主动下载任何照片；"
                    + "如需启用拉取，请配置 file-receiver.pull.* 后重启");
            reportCommonConfig();
            return;
        }

        List<String> missingKeys = new ArrayList<>();
        if (StrUtil.isBlank(pull.getServerUrl())) {
            missingKeys.add("file-receiver.pull.server-url");
        }
        if (StrUtil.isBlank(pull.getAppId())) {
            missingKeys.add("file-receiver.pull.app-id");
        }
        if (StrUtil.isBlank(pull.getAppSecret())) {
            missingKeys.add("file-receiver.pull.app-secret");
        }
        if (!missingKeys.isEmpty()) {
            // 快速失败：终止启动并一次报清全部缺失键，避免逐个试错
            throw new IllegalStateException("拉取模式已启用但缺少必需配置：" + missingKeys + "，请补齐后重启");
        }

        log.info("拉取模式已启用：server-url={}，app-id={}，轮询间隔={}s，app-secret=已配置（不打印）",
                pull.getServerUrl(), pull.getAppId(), pull.getIntervalSeconds());
        reportCommonConfig();
    }

    private void reportCommonConfig() {
        log.info("照片目录 photo-dir={}，过期清理 retention-days={}（0 表示关闭），日志目录={}",
                properties.getPhotoDir(), properties.getCleanup().getRetentionDays(),
                System.getProperty("LOG_DIR", "Logs（相对进程工作目录）"));
    }
}
