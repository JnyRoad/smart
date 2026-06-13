package com.tce.smart.platform.api.dto.req.lock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author sunfujian
 * @date 2021/5/19 11:34
 */
@Data
public class ApiBatchAuthDTO {
    /**
     * 设备ID集合
     */
    @ApiModelProperty(value = "设备ID集合")
    @NotEmpty(message = "设备ID集合不能为空")
    private List<Long> deviceIds;
    /**
     * 人员ID集合
     */
    @ApiModelProperty("人员工号集合")
    @NotEmpty(message = "人员集合不能为空")
    private List<String> badges;
    /**
     * 有效开始时间
     */
    @ApiModelProperty("有效开始时间")
    private LocalDateTime validTimeStart;
    /**
     * 有效结束时间
     */
    @ApiModelProperty("有效结束时间")
    private LocalDateTime validTimeEnd;
}
