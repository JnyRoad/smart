package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约配置表
 *
 * @author fushiping
 * @date 2021-08-13 16:08:16
 */
@Data
public class CommonConfigRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty(value = "ID")
    private Long id;

	@ApiModelProperty(value = "预约类型")
    private Integer businessType;

	@ApiModelProperty(value = "园区id")
    private Integer parkId;

	@ApiModelProperty(value = "配置类型")
    private Integer configType;

	@ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

	@ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

	@ApiModelProperty(value = "配置内容")
	private String value;

}
