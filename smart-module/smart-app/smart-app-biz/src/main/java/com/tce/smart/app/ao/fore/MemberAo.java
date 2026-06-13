package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 随行访客接收参数的信息
 *
 * @author ly
 * @date 2019-05-13 15:13:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2872881423154160334L;

	private String memberName;

	private String memberPhoto;

	private String memberPhotoId;
}
