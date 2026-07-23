package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * 办公区物品放行草稿标识。
 */
@Data
public class OfficeReleaseDraftRespDTO extends BaseDTO {
	private Long releaseId;
}
