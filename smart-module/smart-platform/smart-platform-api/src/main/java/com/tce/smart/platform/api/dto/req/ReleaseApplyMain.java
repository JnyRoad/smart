package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sunfujian
 * @date 2021/8/17 9:39
 */
@Data
public class ReleaseApplyMain extends BaseDTO {
	/**
	 * 放行人级别
	 */
	@ApiModelProperty(value = "放行人级别")
	private String sqrjb = "";

	/**
	 * 附件上传
	 */
	@ApiModelProperty(value = "附件上传")
	private String fjsc = "";

	/**
	 * 安检附件上传
	 */
	@ApiModelProperty(value = "安检附件上传")
	private String ajfjsc = "";

	/**
	 * 出发地点
	 */
	@ApiModelProperty(value = "出发地点")
	private String fxdd = "";

	/**
	 * 到达地点
	 */
	@ApiModelProperty(value = "到达地点")
	private String dddd = "";

	/**
	 * 出发地点详情
	 */
	@ApiModelProperty(value = "出发地点详情")
	private String fxddxq = "";

	/**
	 * 到达地点详情
	 */
	@ApiModelProperty(value = "到达地点详情")
	private String ddddxq = "";

	/**
	 * 物品放行类别
	 */
	@ApiModelProperty(value = "物品放行类别")
	private String wpfxlb = "";

	/**
	 * 是否返厂
	 */
	@ApiModelProperty(value = "是否返厂")
	private String sffc = "";

	/**
	 * 流程编号
	 */
	@ApiModelProperty(value = "流程编号")
	private String lcbh = "";

	/**
	 * 申请人
	 */
	@ApiModelProperty(value = "申请人")
	private String sqr = "";

	/**
	 * 申请部门
	 */
	@ApiModelProperty(value = "申请部门")
	private String sqbm = "";

	/**
	 * 放行事项
	 */
	@ApiModelProperty(value = "放行事项")
	private String fxsx = "";

	/**
	 * 人员放行
	 */
	@ApiModelProperty(value = "人员放行")
	private String ryfx = "";

	/**
	 * 物品放行
	 */
	@ApiModelProperty(value = "物品放行")
	private String wpfx = "";

	/**
	 * 人员放行详情
	 */
	@ApiModelProperty(value = "人员放行详情")
	private String ryfxxq = "";

	/**
	 * 物品放行详情
	 */
	@ApiModelProperty(value = "物品放行详情")
	private String wpfxxq = "";

	/**
	 * 放行去处
	 */
	@ApiModelProperty(value = "放行去处")
	private String fxqc = "";

	/**
	 * 放行事项1
	 */
	@ApiModelProperty(value = "放行事项1")
	private String fxsx1 = "";

}
