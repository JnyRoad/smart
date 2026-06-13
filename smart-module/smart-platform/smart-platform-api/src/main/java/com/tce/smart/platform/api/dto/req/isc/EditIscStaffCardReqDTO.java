package com.tce.smart.platform.api.dto.req.isc;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class EditIscStaffCardReqDTO extends BaseDTO {

	private Long id;

	@NotNull(message = "员工ID不能为空")
	private Long staffId;

	@NotNull(message = "园区不能为空")
	private Integer parkId;

	@NotBlank(message = "ISC卡号不能为空")
	@Pattern(regexp = "[0-9A-Z]{8,20}", message = "ISC卡号必须为8-20位数字或大写字母")
	private String cardNo;
}
