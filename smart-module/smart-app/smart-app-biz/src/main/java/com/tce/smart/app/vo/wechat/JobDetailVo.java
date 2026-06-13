package com.tce.smart.app.vo.wechat;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 招聘岗位详细信息
 *
 * @author qipei
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobDetailVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -2866033942686697760L;

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
	 * 地址
	 */
	private String jobAddress;

	/**
	 * 部门
	 */
	private String jobDept;

	/**
	 * /岗位描述
	 */
	private String jobDesc;

	/**
	 * 职层名称
	 */
	private String jcheName;

	/**
	 * 工资范围
	 */
	private String jobWage;

	/**
	 * 发布时间
	 */
	private String publishDate;

	/**
	 * 有效日期
	 */
	private Date validityDate;

	/**
	 * 园区经度
	 */
	private BigDecimal parkLongitude;
	/**
	 * 园区纬度
	 */
	private BigDecimal parkLatitude;

	/**
	 * 联系方式
	 */
	private String relation;
	/**
	 * 岗位要求
	 */
	private JobNecess jobNecess;

	public JobDetailVo() {
		this.jobNecess = new JobNecess();
	}

	public class JobNecess {
		private String language;

		private String computers;

		private String age;

		private String workYear;

		private String major;

		private String education;

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getComputers() {
			return computers;
		}

		public void setComputers(String computers) {
			this.computers = computers;
		}

		public String getAge() {
			return age;
		}

		public void setAge(String age) {
			this.age = age;
		}

		public String getEducation() {
			return education;
		}

		public void setEducation(String education) {
			this.education = education;
		}

		public String getMajor() {
			return major;
		}

		public void setMajor(String major) {
			this.major = major;
		}

		public String getWorkYear() {
			return workYear;
		}

		public void setWorkYear(String workYear) {
			this.workYear = workYear;
		}

	}

}
