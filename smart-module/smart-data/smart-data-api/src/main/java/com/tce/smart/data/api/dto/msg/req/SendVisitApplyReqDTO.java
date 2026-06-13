package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.util.List;

/**
 * @description: 合肥访客预约
 * @date: 2021/4/1 0001 17:28
 * @author: fushiping
 * @version: 1.0
 */
@Data
public class SendVisitApplyReqDTO {

	/**
	 * 主表
	 */
	private VisitApplyMainReqDTO visitApplyMainReqDTO;

	/**
	 * 人员来访
	 */
	private List<VisitApplyPersonReqDTO> visitApplyPersonReqDTOS;
}
