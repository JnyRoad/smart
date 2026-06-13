package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.core.entity.SmtRecruitmentSetting;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/***
 * description: 招聘配置信息 <br>
 * date: 2019/11/27 14:29 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecruitSetListVO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8384144911227326835L;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 工作地点编号
	 */
	private String workBaseCode;

	/**
	 * 工作地点名称
	 */
	private String workBaseName;

	/**
	 * 签约Bu、单位列表
	 */
	private List<SmtRecruitmentSetting> compOrgList;

}
