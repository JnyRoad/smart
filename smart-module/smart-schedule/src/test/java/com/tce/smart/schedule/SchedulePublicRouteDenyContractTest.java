package com.tce.smart.schedule;

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
import java.util.stream.Stream;
import org.junit.Test;

/**
 * smart-schedule 只在 JVM 内执行定时任务，不能因为 Nacos 匿名白名单重新变成 HTTP 触发入口。
 */
public class SchedulePublicRouteDenyContractTest {

    private static final Pattern LIST_ITEM = Pattern.compile("^\\s*-\\s+([^#\\s]+).*$");
    private static final Pattern HTTP_CONTROLLER = Pattern.compile("@(?:RestController|Controller)\\b");
    private static final Pattern HTTP_MAPPING = Pattern.compile("@(?:RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\\b");

    @Test
    public void scheduleAllowsNoAnonymousHttpRoutes() throws IOException {
        assertEquals(Collections.emptyList(), readIgnoreUrls());
    }

    @Test
    public void scheduleContainsNoHttpControllersOrMappings() throws IOException {
        Path sourceDirectory = locateRepositoryRoot().resolve("smart-module/smart-schedule/src/main/java");
        List<String> httpRouteSources = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> findHttpRouteAnnotations(path, httpRouteSources));
        }
        assertFalse("定时任务服务不得新增 HTTP 路由，新增入口必须另行完成鉴权与路由库存评审：" + httpRouteSources,
                !httpRouteSources.isEmpty());
    }

    private void findHttpRouteAnnotations(Path source, List<String> httpRouteSources) {
        try {
            String content = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
            if (HTTP_CONTROLLER.matcher(content).find() || HTTP_MAPPING.matcher(content).find()) {
                httpRouteSources.add(source.toString());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取调度服务源码：" + source, exception);
        }
    }

    private List<String> readIgnoreUrls() throws IOException {
        Path configPath = locateRepositoryRoot().resolve("docker/nacos/config/dev/smart-schedule.yml");
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
