package com.tce.smart.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * UPMS 本地 Nacos 白名单契约：Open API 必须由已登记客户端及 scope 鉴权，不能整体匿名放行。
 */
public class UpmsOpenApiAccessContractTest {

    private static final Pattern LIST_ITEM = Pattern.compile("^\\s*-\\s+([^#\\s]+).*$" );

    @Test
    public void openApiIsNotAnonymousInIgnoreUrlPolicy() throws IOException {
        List<String> ignoreUrls = readIgnoreUrls("smart-upms-biz.yml");

        assertFalse(ignoreUrls.contains("/api/**"));
        assertEquals(Collections.singletonList("/actuator/health"), ignoreUrls);
    }

    private List<String> readIgnoreUrls(String dataId) throws IOException {
        Path configPath = locateConfig(dataId);
        List<String> urls = new ArrayList<>();
        boolean inIgnoreUrls = false;
        for (String line : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
            if (line.startsWith("spring:")) {
                break;
            }
            if (line.trim().equals("ignore-urls:")) {
                inIgnoreUrls = true;
                continue;
            }
            if (!inIgnoreUrls) {
                continue;
            }
            Matcher matcher = LIST_ITEM.matcher(line);
            if (matcher.matches()) {
                urls.add(matcher.group(1).replace("\"", ""));
            }
        }
        return urls;
    }

    private Path locateConfig(String dataId) {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve("docker/nacos/config/dev").resolve(dataId);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位本地 Nacos 配置：" + dataId);
    }
}
