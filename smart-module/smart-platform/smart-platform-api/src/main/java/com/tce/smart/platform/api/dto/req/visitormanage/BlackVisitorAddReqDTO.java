package com.tce.smart.platform.api.dto.req.visitormanage;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author sunfujian
 * @since 2021/10/29 10:12
 */
@Data
public class BlackVisitorAddReqDTO extends BaseDTO {
	/**
	 * 身份证号
	 */
	@NotBlank(message = "身份证号不能为空")
	private String cardNo;

	/**
	 * 黑名单姓名
	 */
	@NotBlank(message = "姓名不能为空")
	private String personName;

	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 原因
	 */
	@Length(max = 50, message = "原因不能超过50字")
	private String reason;

	@ApiModelProperty("excel导入失败原因")
	private String failReason;
}
