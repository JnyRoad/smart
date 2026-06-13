package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: DormitoryBedReqDTO
 * @Auther: guohongtai

 * @Date: 2020-10-14 22:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryBedReqDTO extends BaseDTO {
	private Integer roomId;
}
