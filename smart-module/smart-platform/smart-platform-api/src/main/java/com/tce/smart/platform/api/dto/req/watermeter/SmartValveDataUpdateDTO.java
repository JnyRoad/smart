package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2021/12/22 10:18
 */
@Data
public class SmartValveDataUpdateDTO extends BaseDTO {

	/**
	 * 集中器ID
	 */
	private String deviceCode;
	/**
	 * 水表序号
	 */
	private String waterMeterSeq;
	/**
	 * 阀门序号
	 */
	private String valveSeq;
	/**
	 * 开关状态
	 */
	private String valveState;
}
