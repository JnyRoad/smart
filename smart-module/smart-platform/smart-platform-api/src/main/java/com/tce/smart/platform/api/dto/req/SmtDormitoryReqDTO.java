package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: SmtDormitoryReqDTO
 * @Auther: guohongtai
 * @Date: 2020-10-14 21:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryReqDTO extends BaseDTO {
	private Integer parkId;

	/**
	 * 是否需要根据用户楼栋权限统计
	 */
	private Boolean isAccount;
}
