package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询App消息推送统计Vo
 *
 * @author mkwu
 * @date 2019-07-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryAppMsgRecVO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3962701279743216972L;

	/**
	 * 消息总条数
	 */
	private long total;

	/**
	 * 已读消息条数
	 */
	private Integer readTotal;

	/**
	 * 未读消息条数
	 */
	private Integer unReadTotal;
}
