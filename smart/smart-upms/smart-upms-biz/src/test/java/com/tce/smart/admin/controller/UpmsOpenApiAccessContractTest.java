package com.tce.smart.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void upmsInternalServiceTokenTemplateAndRolloutChecklistRequireIndependentServerClient() throws IOException {
        String config = new String(Files.readAllBytes(locateConfig("smart-upms-biz.yml")), StandardCharsets.UTF_8);

        String expectedServiceToken = "service-token:\n"
                + "      client-id: \"${SMART_UPMS_OAUTH_CLIENT_ID:}\"\n"
                + "      client-secret: \"${SMART_UPMS_OAUTH_CLIENT_SECRET:}\"\n"
                + "      access-token-uri: \"${SMART_UPMS_OAUTH_TOKEN_URI:}\"";
        assertTrue(config.contains(expectedServiceToken));
        int serviceTokenStart = config.indexOf("service-token:");
        int serviceTokenEnd = config.indexOf("\n    user:", serviceTokenStart);
        String serviceTokenBlock = config.substring(serviceTokenStart, serviceTokenEnd);
        assertFalse(serviceTokenBlock.contains("SMART_OAUTH_CLIENT_ID"));

        String checklist = new String(Files.readAllBytes(locateRepositoryRoot()
                .resolve("docs/security/2026-07-22-yuto-prod-dev-nacos-access-control-rollout.md")),
                StandardCharsets.UTF_8);
        assertTrue(checklist.contains("UPMS 独立 client_credentials"));
        assertTrue(checklist.contains("`server` scope"));
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
        return locateRepositoryRoot().resolve("docker/nacos/config/dev").resolve(dataId);
    }

    private Path locateRepositoryRoot() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("docker/nacos/config/dev"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位仓库根目录");
    }
}
