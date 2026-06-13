package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 工作经验
 * @author qipei
 *
 */
@Data
public class WorkVo {

	/**
	 * 公司名称
	 */
	private String companyName;

	/**
	 * 职位
	 */
	private String jobName;

	/**
	 * 负责人
	 */
	private String prover;

	/**
	 * 负责人电话
	 */
	private String proverMobile;

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;
}
