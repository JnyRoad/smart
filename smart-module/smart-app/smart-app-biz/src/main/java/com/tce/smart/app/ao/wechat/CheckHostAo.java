package com.tce.smart.app.ao.wechat;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 被访人信息查询Ao
 *
 * @author mingkai.wu
 * @date 2019-05-13 08:28:54
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckHostAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8687754044704419673L;

	/**
	 * 被访人姓名
	 */
	private String hostName;

	/**
	 * 被访人电话
	 */
	private String hostMobile;

	/**
	 * 被访人所属园区
	 */
	private Integer parkId;

}
