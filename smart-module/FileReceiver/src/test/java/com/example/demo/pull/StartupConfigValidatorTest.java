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
