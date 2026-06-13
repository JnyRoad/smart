package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/3/18 11:08
 */
@Data
public class DormitoryLazyTreeDTO extends BaseDTO {

	@ApiModelProperty("节点ID")
	private Integer id;

	@ApiModelProperty("名称")
	private String label;

	@ApiModelProperty("是否含有子节点")
	private Boolean hadChild;
}
