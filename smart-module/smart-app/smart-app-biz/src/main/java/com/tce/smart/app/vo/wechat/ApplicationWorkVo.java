package com.tce.smart.app.vo.wechat;

import lombok.Data;

/**
 * 应聘工作经验
 * @author qipei
 *
 */
@Data
public class ApplicationWorkVo {

	private String workHisId;

	/**
	 * 公司
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
	 * 负责人名称
	 */
	private String proverMobile;

	/**
	   * 工作开始时间
	   */
	    private String startTime;
	    /**
	   * 工作结束时间
	   */
	    private String endTime;


}
