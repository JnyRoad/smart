package com.tce.smart.platform.api.dto.req.approval;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: fushiping
 * @Date: 2021-04-08 16:25:24
 */
@Data
public class ApprovalProcessReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 事件id
	 */
	private String businessId;
	/**
	 * 审批id
	 */
	private Integer approvalId;
	/**
	 * 事件类型
	 */
	private Integer eventId;
	/**
	 * 园区id
	 */
	private Integer parkId;
	/**
	 * 宿舍id
	 */
	private Integer dormitoryId;

	/**
	 * 宿舍ids
	 */
	private List<Integer> dormitoryIds;
	/**
	 * 房间号
	 */
	private List<Integer> roomId;
	/**
	 * 退宿原因
	 */
	private Integer quitReason;
	/**
	 * 物品类型
	 */
	private Integer articlesType;

	/**
	 * 发起人工号
	 */
	private String applyBadge;

	/**
	 * 范围类型
	 */
	private Integer rangeType;
	/**
	 * 维修类型 1.灯 2.插座 3.水龙头 4.水管 5.门 6.锁 7.空调
	 */
	private Integer repairType;
	/**
	 * 节点排序
	 */
	private Integer sort;

}
