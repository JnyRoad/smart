package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author sunfujian
 * @date 2021/8/12 20:31
 */
@Data
public class BackFactoryConfirmListDTO extends BaseDTO {
	private Long id;
	@ApiModelProperty(value = "OA单号")
	private String processId;
	@ApiModelProperty(value = "申请人姓名")
	private String name;
	@ApiModelProperty(value = "申请部门")
	private String deptName;
	@ApiModelProperty(value = "放行事项")
	private String releaseItemDesc;
	@ApiModelProperty(value = "放行类别")
	private String releaseTypeDesc;
	@ApiModelProperty(value = "OA节点")
	private String oaNode;
	@ApiModelProperty(value = "申请时间")
	private LocalDateTime createTime;
	@ApiModelProperty(value = "返厂确认状态")
	private String backStatus;
}
