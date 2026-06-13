package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: SmtDormitoryAdministratorRespDTO
 * @Auther: guohongtai
 * @Date: 2020-10-14 20:10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryAdministratorRespDTO extends BaseDTO {

	private Integer parkId;

	private String parkName;

	private String badgeOne;

	private String badgeTwo;

	private String badgeThree;

	private String badgeFour;
}
