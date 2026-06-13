package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: AddDormitoryAdministratorReqDTO
 * @Auther: guohongtai
 * @Date: 2020-10-14 15:29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddDormitoryAdministratorReqDTO extends BaseDTO{
	private Integer parkId;

	private String badgeOne;

	private String badgeTwo;

	private String badgeThree;

	private String badgeFour;
}
