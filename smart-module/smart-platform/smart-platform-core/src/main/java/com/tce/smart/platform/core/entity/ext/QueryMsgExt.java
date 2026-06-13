package com.tce.smart.platform.core.entity.ext;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短息查询条件
 *
 * @author fushiping
 * @date 2019/10/9 16:33
 **/

@Data
public class QueryMsgExt implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6294296002487777282L;

	/**
	 * 电话号码
	 */
	private String phoneNo;

	/**
	 * 开始时间
	 */
	private Date startTime;

	/**
	 * 结束时间
	 */
	private Date endTime;

	/**
	 * 消息发送状态
	 */
	private Integer msgState;

	/**
	 * 模板id
	 */
	private Integer tempId;
}
