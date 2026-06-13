package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 * description: 招聘设置工作地点列表 <br>
 * date: 2019/11/27 14:29 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class RecruitSetCompListVO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3760436629148772825L;

	/**
	 * 签约单位ID
	 */
	private String workOrgId;

	/**
	 * 签约单位全称
	 */
	private String workOrgName;

}
