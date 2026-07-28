package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/** 三方登录配置的普通编辑命令，故意不包含 appSecret，避免查询脱敏后空值覆盖存量密钥。 */
@Data
public class SocialDetailsUpdateReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID不能为空")
    private Integer id;

    @NotBlank(message = "类型不能为空")
    private String type;

    private String remark;

    @NotBlank(message = "账号不能为空")
    private String appId;

    private String redirectUrl;
}
