package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: DormitoryAdministratorReqDTO
 * @Auther: guohongtai
 * @Date: 2020-10-14 20:53
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryAdministratorReqDTO extends BaseDTO {
	private Integer parkId;
}
