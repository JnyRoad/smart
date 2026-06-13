package com.tce.smart.platform.api.dto.req;


import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class VisitJcheLimitReqDTO extends BaseDTO {


	/**
	 * 园区
	 */
	@ApiModelProperty("")
	private Integer parkId;
	/**
	 * 申请原因
	 */
	@ApiModelProperty("")
	private List<String> jcheList;


}
