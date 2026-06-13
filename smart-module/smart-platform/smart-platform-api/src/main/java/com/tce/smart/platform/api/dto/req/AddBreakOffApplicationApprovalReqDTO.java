package com.tce.smart.platform.api.dto.req;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 调休审批申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@Data
public class AddBreakOffApplicationApprovalReqDTO implements Serializable {
	private static final long serialVersionUID = -3563221881568564009L;


	/**
	 *
	 */
	private String staffBadge;

	/**
	 *
	 */
	private Integer restType;
	/**
	 *
	 */
	private Date restDate;
	/**
	 *
	 */
	private Date workDate;
	/**
	 * 调休时长
	 */
	private Integer restCount;
	/**
	 *调休备注
	 */
	private String vacateDesc;

}
