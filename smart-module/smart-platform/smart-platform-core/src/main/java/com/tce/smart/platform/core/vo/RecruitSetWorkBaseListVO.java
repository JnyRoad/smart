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
public class RecruitSetWorkBaseListVO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7996342259679861402L;

	/**
	 * 招聘工作地点编号
	 */
	private String workBaseCode;

	/**
	 * 招聘工作地点名称
	 */
	private String workBaseName;

}
