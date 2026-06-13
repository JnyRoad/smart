package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/3/18 11:36
 */
@Data
public class DormitoryLazyQueryDTO extends BaseDTO {

	@ApiModelProperty("父节点ID(根节点为-1)")
	private Integer parentId;

	@ApiModelProperty("查询类型：1、园区；2、楼栋；3、楼层；4、房间")
	private Integer type;
}
