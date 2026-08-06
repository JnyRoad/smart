package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

/** 上级部门用户选择器所需的最小资料，禁止返回密码、手机号或第三方绑定信息。 */
@Data
public class AncestorUserRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer userId;
	private String username;
	private String fullName;
	private Integer deptId;
}
