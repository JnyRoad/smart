package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@Data
public class StaffTreeRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("键")
	private Integer value;

	@ApiModelProperty("值")
	private String label;

	@ApiModelProperty("子项目")
	private List<StaffTreeRespDTO> children;


}
