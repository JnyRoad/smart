package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author sunfujian
 * @date 2021/8/16 15:41
 */
@Data
public class ReleaseApplyPersonDetail extends BaseDTO {
	private Long id;
	/**
	 * 申请人工号
	 */
	@ApiModelProperty(value = "申请人工号")
	@NotBlank(message = "员工号不能为空")
	private String badge = "";
	/**
	 * 姓名
	 */
	@ApiModelProperty(value = "姓名")
	private String xm = "";
	private String name;

	/**
	 * 工号
	 */
	@ApiModelProperty(value = "工号")
	private String gh = "";

	/**
	 * 离厂事由
	 */
	@ApiModelProperty(value = "离厂事由")
	private String lcsy = "";

	/**
	 * 离厂日期
	 */
	@ApiModelProperty(value = "离厂日期")
	private String lcrq = "";

	/**
	 * 离厂时间
	 */
	@ApiModelProperty(value = "离厂时间")
	private String lcsj = "";

	/**
	 * 返厂日期
	 */
	@ApiModelProperty(value = "返厂日期")
	private String fcrq = "";

	/**
	 * 返厂时间
	 */
	@ApiModelProperty(value = "返厂时间")
	private String fcsj = "";

	/**
	 * 级别
	 */
	@ApiModelProperty(value = "级别")
	private String jb = "";
}
