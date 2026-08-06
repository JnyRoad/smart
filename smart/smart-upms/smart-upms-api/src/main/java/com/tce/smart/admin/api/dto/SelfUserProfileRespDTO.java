package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

/** 当前认证用户可读取的最小个人资料，禁止复用 SysUser 实体对外返回。 */
@Data
public class SelfUserProfileRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private String fullName;
    private String phone;
    private String avatar;
    private Integer deptId;
}
