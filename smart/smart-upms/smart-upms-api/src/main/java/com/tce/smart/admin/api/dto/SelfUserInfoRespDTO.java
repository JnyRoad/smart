package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

/** 当前用户的前端会话投影，不包含密码哈希、openid 或完整 SysUser。 */
@Data
public class SelfUserInfoRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private SelfUserProfileRespDTO profile;
    private String[] permissions;
    private Integer[] roles;
    private String salaryTypeName;
}
