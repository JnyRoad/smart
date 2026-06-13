package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * 保安物品放行确认
 * @author sunfujian
 * @date 2021/8/10 16:50
 */
@Data
public class GuardReleaseConfirmReqDTO extends BaseDTO {
	private Long id;
	private Integer parkId;
	private Integer status;
	private String badge;
	/**
	 * 保安放行上传图片
	 */
	private String guardOneImg;
	private String guardTwoImg;
	private String guardThreeImg;
	private String remark;
}
