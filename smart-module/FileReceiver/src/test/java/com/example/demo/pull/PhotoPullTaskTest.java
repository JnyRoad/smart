package com.example.demo.pull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PhotoPullTask 纯单测：PhotoServerClient 全部 mock，不发起真实网络请求。
 */
public class PhotoPullTaskTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Path photoDir;
    private PhotoServerClient serverClient;
    private OpenApiTokenClient tokenClient;
    private PhotoPullProperties properties;
    private PhotoPullTask task;

    @Before
    public void setUp() throws IOException {
        photoDir = tempFolder.getRoot().toPath();

        serverClient = mock(PhotoServerClient.class);
        tokenClient = mock(OpenApiTokenClient.class);
        when(tokenClient.getToken()).thenReturn("token-1");

        properties = new PhotoPullProperties();
        properties.setPhotoDir(photoDir.toString());

        task = new PhotoPullTask(serverClient, tokenClient, properties);
    }

    @Test
    public void pullTask_downloadsOnlyMissingPhotos() throws IOException {
        // 本地已存在 photo-a.png，服务端清单里的 photo-a 应跳过，只下载 photo-b
        Files.write(photoDir.resolve("photo-a.png"), "existing".getBytes());
        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenReturn(Arrays.asList("photo-a", "photo-b"));
        when(serverClient.downloadPhoto("token-1", "photo-b"))
                .thenReturn(PhotoServerClient.DownloadResult.found("bytes-b".getBytes()));

        task.runOnce();

        verify(serverClient, never()).downloadPhoto(anyString(), eq("photo-a"));
        verify(serverClient, times(1)).downloadPhoto("token-1", "photo-b");
        assertThat(photoDir.resolve("photo-b.png")).exists();
    }

    @Test
    public void pullTask_writesTmpThenAtomicRename() throws IOException {
        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenReturn(Collections.singletonList("photo-c"));

        AtomicInteger finalFileSeenDuringDownload = new AtomicInteger(0);
        when(serverClient.downloadPhoto(anyString(), eq("photo-c"))).thenAnswer(invocation -> {
            // 下载调用发生的那一刻，最终文件还不应存在（此时应处于写 tmp 阶段）
            if (Files.exists(photoDir.resolve("photo-c.png"))) {
                finalFileSeenDuringDownload.incrementAndGet();
            }
            return PhotoServerClient.DownloadResult.found("bytes-c".getBytes());
        });

        task.runOnce();

        assertThat(finalFileSeenDuringDownload.get()).isZero();
        assertThat(photoDir.resolve("photo-c.png")).exists();
        // 完成后目录中不应残留任何 .tmp 半成品文件
        try (Stream<Path> files = Files.list(photoDir)) {
            assertThat(files.map(p -> p.getFileName().toString()))
                    .noneMatch(name -> name.endsWith(".tmp"));
        }
    }

    @Test
    public void pullTask_singleFailureDoesNotAbortRound() {
        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenReturn(Arrays.asList("photo-fail", "photo-ok"));
        when(serverClient.downloadPhoto("token-1", "photo-fail"))
                .thenThrow(new RuntimeException("模拟下载异常"));
        when(serverClient.downloadPhoto("token-1", "photo-ok"))
                .thenReturn(PhotoServerClient.DownloadResult.found("bytes-ok".getBytes()));

        task.runOnce();

        assertThat(photoDir.resolve("photo-fail.png")).doesNotExist();
        assertThat(photoDir.resolve("photo-ok.png")).exists();
    }

    @Test
    public void pullTask_404Skipped() {
        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenReturn(Collections.singletonList("photo-missing"));
        when(serverClient.downloadPhoto("token-1", "photo-missing"))
                .thenReturn(PhotoServerClient.DownloadResult.notFound());

        task.runOnce();

        assertThat(photoDir.resolve("photo-missing.png")).doesNotExist();
        // 404 不应触发 token 刷新重试
        verify(tokenClient, never()).refresh();
    }

    @Test
    public void tokenClient_refreshesOn401Once() {
        // 首次拉清单遇 401，刷新 token 后重试一次即成功
        when(serverClient.fetchPendingPhotoIds("token-1"))
                .thenThrow(new PhotoServerClient.UnauthorizedException("401"));
        when(tokenClient.refresh()).thenReturn("token-2");
        when(serverClient.fetchPendingPhotoIds("token-2"))
                .thenReturn(Collections.emptyList());

        task.runOnce();

        verify(tokenClient, times(1)).refresh();
        verify(serverClient, times(1)).fetchPendingPhotoIds("token-1");
        verify(serverClient, times(1)).fetchPendingPhotoIds("token-2");
    }
}
