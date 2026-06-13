package com.tce.smart.platform.api.dto.resp.dormitoryconfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:53
 */
@Data
public class DormitoryConfigListRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "ID")
	private Long id;

	@ApiModelProperty(value = "园区id")
	private Integer parkId;

	@ApiModelProperty(value = "园区名")
	private String parkName;

	@ApiModelProperty(value = "创建时间")
	private LocalDateTime createTime;

}
