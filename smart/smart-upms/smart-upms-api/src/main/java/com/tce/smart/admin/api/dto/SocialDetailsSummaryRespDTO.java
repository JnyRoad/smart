package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 社交客户端管理列表投影，绝不回传 appSecret。 */
@Data
public class SocialDetailsSummaryRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String type;
    private String remark;
    private String appId;
    private String redirectUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
