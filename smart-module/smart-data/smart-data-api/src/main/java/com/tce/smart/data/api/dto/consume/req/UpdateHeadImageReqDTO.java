package com.tce.smart.data.api.dto.consume.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送短信Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateHeadImageReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7033702649865974034L;

	public UpdateHeadImageReqDTO() {
	}

	public UpdateHeadImageReqDTO(String badge, String headImage) {
		this.badge = badge;
		this.headImage = headImage;
	}

	/**
	 * 工号
	 */
	private String badge;

	/**
	 * 头像
	 */
	private String headImage;

}
