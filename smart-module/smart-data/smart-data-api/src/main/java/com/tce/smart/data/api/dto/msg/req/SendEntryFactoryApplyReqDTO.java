package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.util.List;

/**
 * @description: 入厂申请
 * @date: 2021/4/1 0001 17:28
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SendEntryFactoryApplyReqDTO {

	/**
	 * 主表
	 */
	private EntryFactoryApplyMainReqDTO entryFactoryApplyMainReqDTO;

	/**
	 * 分子公司人员来访
	 */
	private List<EntryFactoryApplyShortDetailReqDTO> entryFactoryApplyShortDetailReqDTOs;

	/**
	 * 外部人员来访
	 */
	private List<EntryFactoryApplyLongDetailReqDTO> entryFactoryApplyLongDetailReqDTOs;

	/**
	 * 车辆通行办理明细表
	 */
	private List<EntryFactoryApplyCarDetailReqDTO> entryFactoryApplyCarDetailReqDTOs;
}
