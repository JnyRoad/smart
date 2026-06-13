package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import com.tce.smart.platform.core.entity.SmtRecruitmentSetting;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/***
 * description: 招聘设置查询条件 <br>
 * date: 2019/11/27 15:21 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecruitSetSaveAO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8434367407001156524L;

	/**
	 * 园区ID
	 */
	@NotNull(message = "园区ID不能为空")
	private Integer parkId;

	/**
	 * 签约Bu、单位列表
	 */
	@NotNull(message = "签约BU、单位不能为空")
	@NotEmpty(message = "签约BU、单位不能为空")
	private List<SmtRecruitmentSetting> compOrgList;

}
