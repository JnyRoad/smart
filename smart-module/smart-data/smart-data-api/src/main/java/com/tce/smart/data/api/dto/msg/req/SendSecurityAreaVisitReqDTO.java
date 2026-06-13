package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.util.List;

/**
 * @description: SendSecurityAreaVisitReqDTO
 * @date: 2021/4/1 0001 17:28
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SendSecurityAreaVisitReqDTO {

	/**
	 * 主表
	 */
	private SecurityAreaVisitMainReqDTO securityAreaVisitMainReqDTO;

	/**
	 * 明细表
	 */
	private List<SecurityAreaVisitDetailReqDTO> securityAreaVisitDetailReqDTOs;
}
