package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UpApplicationListReqDTO implements Serializable {

	/**
	 * 批量修改的application  id
	 */
	private List<Long> ids;

	//0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职/6-已入库
	private Integer status;

	/**
	 *
	 *
	 * 如果修改为面试，此字段有效，为面试时间
	 */
	private Date interviewTime;

	/**
	 * 如果修改为拒绝，此字段为拒绝原因
	 */
	private String refuseReason;

	/**
	 * 操作人姓名
	 */
	private String createUserNme;


}
