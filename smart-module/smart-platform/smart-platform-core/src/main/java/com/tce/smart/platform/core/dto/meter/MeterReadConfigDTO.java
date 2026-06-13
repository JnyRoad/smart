package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author Li.JiaJun
 * @since 2022/7/1 15:51
 */
@Data
public class MeterReadConfigDTO extends BaseDTO {

	private Date startDate;

	private Date endDate;
}
