package com.tce.smart.schedule.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.io.Serializable;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Li.ShiXun
 * @since 2024/9/4 15:49
 */
@Data
public class AuthItemListDTO implements Serializable {

    private String personId;

    /**
     * 人员状态
     * 0 已配置未下载, 1 更新待下载, 2 更新待删除, 3 已下载, 4 未配置
     */
    private Integer personStatus;

    /**
     * 卡片状态
     * 0 已配置未下载, 1 更新待下载, 2 更新待删除, 3 已下载, 4 未配置
     */
    private Integer cardStatus;

    /**
     * 人脸状态
     * 0 已配置未下载, 1 更新待下载, 2 更新待删除, 3 已下载, 4 未配置
     */
    private Integer faceStatus;

    private JSONObject personDownloadDetail;
}
