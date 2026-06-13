package com.tce.smart.app.ao.wechat;

import lombok.Data;

/**
 * 应聘者提交工作经验
 * @author qipei
 *
 */
@Data
public class ApplicationWorkAo {


	private String companyName;

	private String jobName;

	private String prover;

	private String proverMobile;

	private String startTime;

	private String endTime;


}
