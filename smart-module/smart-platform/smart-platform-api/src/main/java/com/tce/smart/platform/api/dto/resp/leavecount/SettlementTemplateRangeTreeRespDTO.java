package com.tce.smart.platform.api.dto.resp.leavecount;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 * @author lijiajun
 * @date 2022-08-09 11:01:50
 */
@Data
public class SettlementTemplateRangeTreeRespDTO extends BaseDTO {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("房间/楼栋id")
	private Integer id;

	@ApiModelProperty("名称")
	private String label;

	@ApiModelProperty("是否选中")
	private Boolean checked;

	@ApiModelProperty("是否可用")
	private Boolean disabled;

	@ApiModelProperty("子集")
	private List<SettlementTemplateRangeTreeRespDTO> children;

}
