package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * App 密码找回完成后提交给 UPMS 的最小内部命令。
 *
 * <p>授权码只能放在请求体中，由 UPMS 按账号的一次性 Redis 状态校验和消费，不能作为 URL 参数或日志字段。</p>
 */
@Data
public class InternalPasswordResetReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "新密码不能为空")
    private String password;

    @NotBlank(message = "密码修改授权不能为空")
    private String updateAuthCode;
}
