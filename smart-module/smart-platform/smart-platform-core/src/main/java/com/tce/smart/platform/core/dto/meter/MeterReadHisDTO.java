package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/7/14 16:27
 */
@Data
public class MeterReadHisDTO extends BaseDTO {
	/**
	 * 读数
	 */
	private String currentReading;
}
