package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 招聘岗位列表Vo
 *
 * @author Administrator
 *
 */
@Data
public class JobListRespDTO implements Serializable {

	private static final long serialVersionUID = 843105702551352738L;

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
