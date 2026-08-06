package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建办公区物品放行草稿的请求。
 *
 * 申请人始终由认证主体决定，浏览器只能指定其有数据权限的园区。
 */
@Data
public class CreateOfficeReleaseDraftReqDTO extends BaseDTO {
	@NotNull(message = "园区不能为空")
	private Integer parkId;
}
