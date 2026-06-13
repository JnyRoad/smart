package com.tce.smart.platform.api.dto.req.remoteLock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author fushiping
 * @date 2021/7/9 15:20
 *
 */
@Data
@Builder
public class PersonUpdateDTO {

    @ApiModelProperty(value = "旧员工号")
    @NotBlank(message = "旧员工号不能为空")
    private String personNum;

    @ApiModelProperty(value = "员工姓名")
    @NotBlank(message = "员工姓名不能为空")
    private String personName;

    @ApiModelProperty(value = "新员工号")
    private String newPersonNum;
    @ApiModelProperty(value = "新员工手机号")
    private String newPersonPhone;
}
