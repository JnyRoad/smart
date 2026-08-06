package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 平台内部管理流程使用的用户摘要，不包含密码、手机号或任何 OAuth 资料。 */
@Data
public class InternalUserSummaryRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private List<String> roleNames;
}
