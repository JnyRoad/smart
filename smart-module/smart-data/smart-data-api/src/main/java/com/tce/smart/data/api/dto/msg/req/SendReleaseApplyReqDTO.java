package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.util.List;

/**
 * @description: 放行条申请
 * @date: 2021/4/1 0001 17:28
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SendReleaseApplyReqDTO {

	/**
	 * 主表
	 */
	private ReleaseApplyMainReqDTO releaseApplyMainReqDTO;

	/**
	 * 人员放行明细表
	 */
	private List<ReleaseApplyPersonDetailReqDTO> releaseApplyPersonDetailReqDTOs;

	/**
	 * 物品放行明细表
	 */
	private List<ReleaseApplyThingDetailReqDTO> releaseApplyThingDetailReqDTOs;
}
