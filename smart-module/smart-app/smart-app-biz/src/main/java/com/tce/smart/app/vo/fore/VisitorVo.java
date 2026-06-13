package com.tce.smart.app.vo.fore;

import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;

	/**
	 * 访客id
	 */
	private String visitId;
	/**
	 * 访客姓名
	 */
	private String visitorName;
	/**
	 * 来访访客图片信息
	 */
	private String visitorPhoto;
	/**
	 * 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
	 */
	private Integer visitState;
	/**
	 * 预约状态
	 */
	private String visitStateDesc;

	/**
	 * 来访原因
	 */
	private String visitReason;
	/**
	 * 开始时间
	 */
	private Date startDate;
	/**
	 * 结束时间
	 */
	private Date endDate;

	/**
	 * 审批节点名称
	 */
	private String processName;

	private String parkName;

	private Integer parkId;

}
