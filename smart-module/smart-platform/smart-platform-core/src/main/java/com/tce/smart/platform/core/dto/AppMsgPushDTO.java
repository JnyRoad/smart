package com.tce.smart.platform.core.dto;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App消息推送业务字段封装基础类
 *
 * @author mkwu
 * @date 2019-07-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppMsgPushDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 9156658687019331104L;

	/**
	 * 员工号(接收APP)
	 */
	private String badge;
	/**
	 * 员工号(申请)
	 */
	private String applicant;

	/**
	 * 模板编码
	 */
	private String templateCode;

	/**
	 * 业务ID
	 */
	private String bussiessId;

	/**
	 * 连接
	 */
	private String url;

	/**
	 * 业务自定义参数 ,多个以"||"分开
	 */
	private String extraParam;

	/**
	 * 传输内容
	 */
	private String content;



}
