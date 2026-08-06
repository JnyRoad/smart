package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/** 三方登录 appSecret 的显式轮换命令，不与普通资料编辑混用。 */
@Data
public class SocialDetailsSecretRotateReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "密钥不能为空")
    private String appSecret;
}
