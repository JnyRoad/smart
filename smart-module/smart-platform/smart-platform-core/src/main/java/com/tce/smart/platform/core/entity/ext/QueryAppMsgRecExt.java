package com.tce.smart.platform.core.entity.ext;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * App消息推送记录查询条件
 *
 * @author mkwu
 * @date 2019-07-08
 */
@Data
public class QueryAppMsgRecExt implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -6332805492338905682L;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 设备编号
	 */
	private String deviceNo;

	/**
	 * 查询开始时间
	 */
	private Date startTime;

	/**
	 * 查询结束时间
	 */
	private Date endTime;

}
