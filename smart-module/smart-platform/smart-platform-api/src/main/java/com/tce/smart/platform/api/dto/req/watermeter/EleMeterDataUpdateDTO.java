package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author sunfujian
 * @since 2021/11/10 18:10
 */
@Data
public class EleMeterDataUpdateDTO extends BaseDTO {
	/**
	 * 集中器ID
	 */
	private String deviceCode;
	/**
	 * 电表序号
	 */
	private Integer eleMeterSeq;
	/**
	 * 电表当前读数
	 */
	private String eleMeterCurrVal;
	/**
	 * 采集时间
	 */
	private String collectTime;
	/**
	 * 集中器为单条读数生成的可重放事件标识。
	 */
	private String sourceEventId;
}
