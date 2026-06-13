package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加职工出差申请
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
public class AddSmtTravelApplicationReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 *
	 */
	private String staffBadge;

	/**
	 *
	 */
	private String startDate;
	/**
	 *
	 */
	private String endDate;
	/**
	 * 出差时长
	 */
	private String travelCount;

	/**
	 * 出差的地方
	 */
	private String travelCity;
	/**
	 * 原因
	 */
	private String travelDesc;

}
