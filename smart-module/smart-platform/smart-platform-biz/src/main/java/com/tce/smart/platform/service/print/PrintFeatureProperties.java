package com.tce.smart.platform.service.print;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

/** 权限键、分类与内部渲染地址必须由目标环境显式配置，不假定已有角色获权。 */
@Data
@Component
@ConfigurationProperties(prefix = "smart.print")
public class PrintFeatureProperties {
    private boolean enabled;
    private Map<String, String> permissions = new HashMap<>();
    private Map<String, List<String>> classificationCodes = new HashMap<>();
    private List<String> allowedFonts = new ArrayList<>(Collections.singletonList("NotoSansSC"));
    private String rendererUrl;
    @lombok.ToString.Exclude
    private String rendererToken;
    private int rendererConnectTimeoutMs = 3000;
    private int rendererReadTimeoutMs = 30000;
}
