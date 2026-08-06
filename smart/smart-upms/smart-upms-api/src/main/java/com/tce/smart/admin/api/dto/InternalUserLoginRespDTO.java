package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 认证服务内部使用的登录资料投影。
 *
 * <p>该对象只允许通过受管服务令牌取得，不能替代对外用户资料接口；特意不包含手机号、头像、openid、时间戳等资料。</p>
 */
@Data
public class InternalUserLoginRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private Integer deptId;
    private String username;
    private String passwordHash;
    private String lockFlag;
    private Integer[] roleIds;
    private String[] permissions;
    private String salaryTypeName;
    private List<Integer> parkIds;
}
