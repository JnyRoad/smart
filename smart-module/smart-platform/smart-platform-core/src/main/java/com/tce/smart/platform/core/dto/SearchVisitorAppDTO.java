package com.tce.smart.platform.core.dto;


import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客表app
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchVisitorAppDTO extends Model<SearchVisitorAppDTO> {
	private static final long serialVersionUID = 1L;


	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	/**
	 * 被访人员工号
	 */
	private String receptionistBadge;
	/**
	 * 发起人的员工号
	 */
	private String promoterBadge;

	private Integer status;// 状态


}
