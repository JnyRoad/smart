package com.example.demo.pull;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * StartupConfigValidator 纯单测：验证启动期配置自检的快速失败行为。
 */
public class StartupConfigValidatorTest {

    private PhotoPullProperties properties;
    private StartupConfigValidator validator;

    @Before
    public void setUp() {
        properties = new PhotoPullProperties();
        validator = new StartupConfigValidator(properties);
    }

    @Test
    public void pullDisabled_startsWithoutError() {
        // 拉取未启用：只打 WARN 提示，不阻断启动（兼容仅跑旧推送接口的过渡期部署）
        properties.getPull().setEnabled(false);

        assertThatCode(() -> validator.validateAndReport()).doesNotThrowAnyException();
    }

    @Test
    public void pullEnabledWithMissingConfig_failsFastListingMissingKeys() {
        // 拉取已启用但关键配置缺失：启动即失败，报错信息点名缺哪些键，避免上线后静默空转
        properties.getPull().setEnabled(true);
        properties.getPull().setServerUrl("  ");
        properties.getPull().setAppId(null);
        properties.getPull().setAppSecret("");

        assertThatThrownBy(() -> validator.validateAndReport())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("file-receiver.pull.server-url")
                .hasMessageContaining("file-receiver.pull.app-id")
                .hasMessageContaining("file-receiver.pull.app-secret");
    }

    /**
     * 清理间隔为 0/负数时必须启动即失败并点名配置键：否则会漏到 @Scheduled 注册阶段
     * 抛不含任何配置键名的 IllegalArgumentException，远程 Windows 现场无从排查。
     * （0 不承担「关闭清理」语义——关闭请用 retention-days=0，报错信息里要引导到位。）
     */
    @Test
    public void cleanupIntervalNotPositive_failsFastNamingKey() {
        properties.getPull().setEnabled(false);

        properties.getCleanup().setIntervalSeconds(0);
        assertThatThrownBy(() -> validator.validateAndReport())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("file-receiver.cleanup.interval-seconds")
                .hasMessageContaining("retention-days=0");

        properties.getCleanup().setIntervalSeconds(-300);
        assertThatThrownBy(() -> validator.validateAndReport())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("file-receiver.cleanup.interval-seconds");
    }

    /** 拉取轮询间隔为 0/负数同样必须启动即失败：@Scheduled 不论 enabled 与否都会注册，0 间隔照样炸启动 */
    @Test
    public void pullIntervalNotPositive_failsFastNamingKey() {
        properties.getPull().setEnabled(false);
        properties.getPull().setIntervalSeconds(0);

        assertThatThrownBy(() -> validator.validateAndReport())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("file-receiver.pull.interval-seconds")
                .hasMessageContaining("enabled=false");
    }

    /** 多个配置问题必须合并成一次报错点清（本类设计哲学），避免现场修一轮、重启一轮地逐个试错 */
    @Test
    public void multipleConfigProblems_reportedInSingleFailure() {
        properties.getPull().setEnabled(true);
        properties.getPull().setServerUrl("");
        properties.getPull().setAppId("file-receiver-xc");
        properties.getPull().setAppSecret("secret");
        properties.getCleanup().setIntervalSeconds(0);

        assertThatThrownBy(() -> validator.validateAndReport())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("file-receiver.cleanup.interval-seconds")
                .hasMessageContaining("file-receiver.pull.server-url");
    }

    @Test
    public void pullEnabledWithCompleteConfig_startsWithoutError() {
        properties.getPull().setEnabled(true);
        properties.getPull().setServerUrl("http://gateway:9990");
        properties.getPull().setAppId("file-receiver-xc");
        properties.getPull().setAppSecret("secret");

        assertThatCode(() -> validator.validateAndReport()).doesNotThrowAnyException();

        // 校验通过不应改动配置本身
        assertThat(properties.getPull().getServerUrl()).isEqualTo("http://gateway:9990");
    }
}
