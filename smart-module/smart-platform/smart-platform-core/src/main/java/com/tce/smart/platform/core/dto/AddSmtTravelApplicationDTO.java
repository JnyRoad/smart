package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加职工出差申请
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddSmtTravelApplicationDTO extends Model<AddSmtTravelApplicationDTO> {
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
