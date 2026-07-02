package com.tce.smart.tool;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import com.tce.smart.tool.util.HuaweiOBSUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 华为 OBS 集成测试：依赖真实 OBS 凭据与网络，
 * 未配置凭据的环境（本地开发机 / CI）会整体跳过而不是失败。
 */
@Slf4j
public class HuaweiOBSTest {

    /**
     * 凭据缺失时通过 JUnit Assume 跳过全部用例；
     * 配置方式与 HuaweiOBSUtil 一致：系统属性 smart.obs.ak/sk 或环境变量 SMART_OBS_AK/SK。
     */
    @Before
    public void skipWhenObsCredentialsAbsent() {
        Assume.assumeTrue("未配置 OBS 凭据（smart.obs.ak/sk 或 SMART_OBS_AK/SK），跳过集成测试",
                hasConfig("smart.obs.ak", "SMART_OBS_AK") && hasConfig("smart.obs.sk", "SMART_OBS_SK"));
    }

    private static boolean hasConfig(String propertyKey, String envKey) {
        String value = System.getProperty(propertyKey, System.getenv(envKey));
        return value != null && !value.trim().isEmpty();
    }

    @Test
    public void getListObject() {
        List<String> fileNameList = HuaweiOBSUtil.readLastRemoteImgNameList(LocalDateTime.now());
        System.out.println("获取到数量：" + (long) fileNameList.size());
        for (String fileName : fileNameList) {
            System.out.println(fileName);
        }
    }

    @Test
    public void getStaffPhoto() {
        String result = HuaweiOBSUtil.readRemoteImgToBase64("41108219891004903X1");
        if (result.isEmpty()) {
            log.info("未获取到照片");
            return;
        }

        log.info("result:{}", result);
    }
}
