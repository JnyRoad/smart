package com.tce.smart.admin.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/** 平台创建园区企业管理员时的最小内部命令。 */
@Data
public class InternalParkAdminProvisionReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private Integer parkId;

    @NotNull
    private Integer roleId;
}
