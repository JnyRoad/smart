package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.util.List;

/**
 * 办公区物品放行
 * @author sunfujian
 * @date 2021/8/9 20:40
 */
@Data
public class OfficeZoneReleaseReqDTO extends BaseDTO {

	private String badge;
	private Integer parkId;
	private String oneImg;
	private String twoImg;
	private String threeImg;

	/**
	 * 主表
	 */
	private ReleaseApplyMain applyMain;

	/**
	 * 人员放行明细表
	 */
	private List<ReleaseApplyPersonDetail> personList;

	/**
	 * 物品放行明细表
	 */
	private List<ReleaseApplyThingDetail> thingList;
}
