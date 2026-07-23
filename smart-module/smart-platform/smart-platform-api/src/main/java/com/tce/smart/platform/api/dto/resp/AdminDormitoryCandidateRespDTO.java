package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 宿舍分配候选员工的管理端最小投影。
 *
 * 证件号、手机号和 BU 内部标识均不属于床位分配所必需的信息，不能随列表返回。
 */
@Data
public class AdminDormitoryCandidateRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String badge;
	private String jobName;
	private String compName;
	private String depName;
	private String jcheName;
	private Date createTime;
	private String welfareLevel;
	private String parkName;
}
