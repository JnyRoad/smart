package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App消息推送记录列表Vo
 *
 * @author mckaywu
 * @date 2019-06-11 15:39:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppMsgPushListVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7364138655259907081L;

	/**
	 * 记录ID
	 */
	private Integer recordId;

	/**
	 * 业务ID
	 */
	private String businessId;

	/**
	 * 业务类型
	 */
	private Integer businessType;

	/**
	 * 消息标题
	 */
	private String msgTitle;

	/**
	 * 消息内容
	 */
	private String msgContent;

	/**
	 * 是否已阅读 0-未读，1-已读
	 */
	private Integer readState;

	/**
	 * 是否已删除：0-删除，1-正常
	 */
	private Integer deleteState;

	/**
	 * 业务自定义参数 ,多个以"||"分开
	 */
	private String extraParam;

	/**
	 * 发送日期
	 */
	private String createDate;

	/**
	 * 发送时间
	 */
	private String createTime;

}