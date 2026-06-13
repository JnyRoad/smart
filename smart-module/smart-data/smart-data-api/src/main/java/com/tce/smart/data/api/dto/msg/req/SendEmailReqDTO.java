package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 发送面试邀请邮件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendEmailReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3098643407319546387L;

	/**
	 * 收件箱
	 */
	private String inbox;

	/**
	 * 邮件模板号
	 */
	private String tempCode;

	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 模板号对应的变量及值,key为变量名
	 *  {姓名：value，岗位：value}
	 */
	private Map<String,String> param;

	/**
	 * 附件
	 */
	private List<EmailFileReqDTO> fileData;

}
