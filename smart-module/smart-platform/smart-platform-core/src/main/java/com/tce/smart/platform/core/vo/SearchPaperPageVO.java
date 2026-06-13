package com.tce.smart.platform.core.vo;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class SearchPaperPageVO extends BaseVO {

private Integer id;

	/**
	 * 问卷标题
	 */
	private String title;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 状态 0-未开始 1-进行中 2-已结束
	 */
	private Integer status;

	/**
	 * 创建者
	 */
	private String createUser;


	/**
	 * 所属园区id
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 发布范围的名称，以，分割
	 */
	private String compNames;
}
