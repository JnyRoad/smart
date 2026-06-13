package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 申请区域
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityAuthApplyDetailAreaReqDTO {
	/**
	 * 申请进入区域
	 */
	private String sqjrqy;
}
