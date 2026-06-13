package com.tce.smart.platform.api.dto.req.isc;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class EditIscParkConfigReqDTO extends BaseDTO {

	private Long id;

	@NotNull(message = "园区不能为空")
	private Integer parkId;

	@NotNull(message = "ISC调度园区不能为空")
	private Integer dispatcherParkId;

	@NotNull(message = "卡片同步开关不能为空")
	@Range(min = 0, max = 1, message = "卡片同步开关必须是0或1")
	private Integer cardSyncEnabled;

	@Size(max = 512, message = "备注不能超过512个字符")
	private String remark;
}
