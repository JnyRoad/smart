package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * 物品放行人员选择的最小响应。
 *
 * 该 DTO 不能增加证件、手机号、地址、人脸或任何完整员工档案字段。
 */
@Data
public class ReleaseStaffLookupRespDTO extends BaseDTO {
	private Integer id;
	private String name;
}
