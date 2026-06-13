package com.tce.smart.platform.api.dto.req.lock;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 *
 * @author sunfujian
 * @date 2021/6/16 15:44
 */
@Data
public class ApiEditAuthDTO {
    @ApiModelProperty(value = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Long id;
    /**
     * 有效开始时间
     */
    @ApiModelProperty("有效开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validTimeStart;
    /**
     * 有效结束时间
     */
    @ApiModelProperty("有效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validTimeEnd;
}
