package com.tce.smart.platform.api.dto.req.news;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 17:59:47
 */
@Data
public class NewsTerminalReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
    private Long id;

	@ApiModelProperty(value = "终端名")
	@NotBlank(message = "终端名不能为空")
    private String name;

	@ApiModelProperty(value = "IP")
	@NotBlank(message = "IP不能为空")
    private String ip;

	@ApiModelProperty(value = "备注")
    private String remark;

	@ApiModelProperty(value = "信息id")
    private Long infoId;

	@ApiModelProperty(value = "发布时效类型")
	private Integer timeType;

	@ApiModelProperty(value = "生效开始时间")
	private LocalDateTime startTime;

}
