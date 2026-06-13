package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.util.List;

/**
 * @description: 保密权限申请表
 * @date: 2021/4/1 0001 17:28
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SendSecurityAuthApplyReqDTO {

	/**
	 * 主表
	 */
	private SecurityAuthApplyMainReqDTO securityAuthApplyMainReqDTO;

	/**
	 * 授权人员明细表
	 */
	private List<SecurityAuthApplyDetailReqDTO> securityAuthApplyDetailReqDTOs;

	/**
	 * 申请区域
	 */
	private List<SecurityAuthApplyDetailAreaReqDTO> securityAuthApplyDetailAreaReqDTOS;
}
