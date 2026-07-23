package com.tce.smart.platform.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * Platform 本地 Nacos 白名单契约：员工和放行业务路由必须经过认证，不能因配置回退为匿名访问。
 */
public class PublicRouteDenyContractTest {

    private static final Pattern LIST_ITEM = Pattern.compile("^\\s*-\\s+([^#\\s]+).*$" );

    @Test
    public void platformAllowsOnlyHealthAndCapabilityGuardedPublicRoutes() throws IOException {
        List<String> ignoreUrls = readIgnoreUrls("smart-platform.yml");

        assertEquals(Arrays.asList(
                "/actuator/health",
                "/admittance/apply/get/openId",
                "/admittance/visitor-face/capability",
                "/admittance/visitor-face/crop",
                "/regist/save/identification",
                "/regist/face/crop",
                "/regist/face/add"), ignoreUrls);
        assertFalse("公开例外必须逐路径声明，不能扩展整个简历前缀", ignoreUrls.contains("/regist/**"));
    }

    @Test
    public void publicLegacyStaffAndReleasePathsAreAbsentFromIgnoreUrlPolicy() throws IOException {
        List<String> ignoreUrls = readIgnoreUrls("smart-platform.yml");

        assertFalse(ignoreUrls.contains("/staff/**"));
        assertFalse(ignoreUrls.contains("/staff/simple/badge"));
        assertFalse(ignoreUrls.contains("/articlesrelease/**"));
        assertFalse(ignoreUrls.contains("/inner/**"));
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
