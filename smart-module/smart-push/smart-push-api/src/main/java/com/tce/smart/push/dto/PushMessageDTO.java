package com.tce.smart.push.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 单条消息推送
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
@Data
public class PushMessageDTO implements Serializable {

    private static final long serialVersionUID = -4756120585604395701L;
    /**
     * 推送标题
     */
    private String title;

    /**
     * 推送内容
     */
    private String content;

    /**
     * 推送扩展
     */
    private String payload;

    /**
     * 打开链接
     */
    private String url;
}
