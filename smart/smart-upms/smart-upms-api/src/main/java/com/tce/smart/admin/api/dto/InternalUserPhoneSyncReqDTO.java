package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/** 内部手机号同步请求，只允许同步账号与手机号两个字段。 */
@Data
public class InternalUserPhoneSyncReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String username;

    @NotBlank
    private String phone;
}
