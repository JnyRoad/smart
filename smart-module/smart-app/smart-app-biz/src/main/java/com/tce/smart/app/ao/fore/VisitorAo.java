package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客接收参数的信息
 *
 * @author ly
 * @date 2019-05-13 15:13:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2872881423154160334L;

	/**
	 * 来访类型
	 */
	private Integer visitListType;
}
