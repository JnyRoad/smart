package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * @Auther: guohongtai
 * @Date: 2020-08-09 21:13
 */
@Data
public class AllowanceStatusRespDTO extends BaseVO {
	private Integer accommodationStatus;

	private Integer mealStatus;
}
