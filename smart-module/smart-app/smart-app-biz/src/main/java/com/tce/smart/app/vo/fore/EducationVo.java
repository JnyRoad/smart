package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 教育经验
 * @author qipei
 *
 */
@Data
public class EducationVo {

	/**
	 * 学校
	 */
	private String schoolName;

	/**
	 * 专业
	 */
	private String major;

	/**
	 * 学历
	 */
	private String education;

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;
}
