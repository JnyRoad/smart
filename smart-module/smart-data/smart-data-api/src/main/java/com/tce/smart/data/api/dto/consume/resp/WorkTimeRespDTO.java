package com.tce.smart.data.api.dto.consume.resp;

import cn.hutool.core.date.DateTime;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 节假日时间表
 *
 * @author fushiping
 * @date 2020-7-09
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class WorkTimeRespDTO extends BaseDTO {

    /**
	 * 序列号
	 */
	private static final long serialVersionUID = 2580911956654134951L;

	private List<Date> times;
}
