package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 招聘岗位列表Vo
 *
 * @author Administrator
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobListVO extends BaseVO {

	private static final long serialVersionUID = -1L;

	/**
	 * 招聘岗位ID
	 */
	private Integer recruitId;

	/**
	 * 岗位名称
	 */
	private String jobName;

	/**
	 * 招聘人数
	 */
	private Integer jobCount;

	/**
	 * 工作地址
	 */
	private String jobAddress;

	/**
	 * 工资范围
	 */
	private String jobWage;

	/**
	 * 发布日期
	 */
	private String publishDate;

	/**
	 * 公司名称
	 */
	private String compName;
}
