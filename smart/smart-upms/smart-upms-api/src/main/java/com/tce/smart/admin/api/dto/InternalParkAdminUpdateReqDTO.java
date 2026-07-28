package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/** 平台更新园区企业管理员时的最小内部命令。 */
@Data
public class InternalParkAdminUpdateReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer userId;

    @NotBlank
    private String username;

    private String password;

    @NotNull
    private Integer parkId;
}
