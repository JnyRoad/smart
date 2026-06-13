package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author sunfujian
 * @since 2021/10/11 9:23
 */
@Data
public class OaStaffInfoRespDTO extends BaseDTO {
	/**
	 * ID
	 */
	private Integer id;

	/**
	 * 员工名称
	 */
	private String name;
}
