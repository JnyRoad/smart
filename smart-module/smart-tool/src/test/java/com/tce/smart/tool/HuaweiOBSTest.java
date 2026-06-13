package com.tce.smart.tool;

import cn.hutool.core.date.DateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import com.tce.smart.tool.util.HuaweiOBSUtil;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class HuaweiOBSTest {
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
