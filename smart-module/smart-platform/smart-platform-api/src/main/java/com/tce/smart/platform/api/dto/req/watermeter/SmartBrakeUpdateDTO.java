package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/5/11 14:31
 */
@Data
public class SmartBrakeUpdateDTO extends BaseDTO {

	/**
	 * 集中器ID
	 */
	private String deviceCode;
	/**
	 * 电表序号
	 */
	private String eleMeterSeq;
	/**
	 * 开关状态
	 */
	private String brakeState;
}
