package com.tce.smart.platform.api.dto.req.lock;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author sunfujian
 * @since 2021/10/21 11:00
 */
@Data
public class DeviceBindReqDTO extends BaseDTO {
    @NotNull(message = "设备ID不能为空")
    private Long id;
    @NotNull(message = "房间Id不能为空")
    private Long roomId;
    @NotBlank(message = "区域名称不能为空")
    private String areaName;
}
