package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 * description: 招聘设置保存 <br>
 * date: 2019/11/27 15:21 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecruitSetSearchAO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8542221635205640840L;

	/**
	 * Bu名称
	 */
	private String compTitle;

	/**
	 * 签约单位名称
	 */
	private String workOrgName;

	/**
	 * 工作地 点名称
	 */
	private String workBaseName;
}
