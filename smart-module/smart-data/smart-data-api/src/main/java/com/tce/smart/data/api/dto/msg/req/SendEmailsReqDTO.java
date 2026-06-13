package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 批量发送面试邀请邮件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendEmailsReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8882394470088022848L;


	/**
	 * 邮件模板号
	 */
	private String tempCode;

	/**
	 * 模板号对应的变量及值,key为变量名
	 *  {姓名：value，岗位：value}
	 */
	private Map<String,String> param;

	/**
	 * 批量收件人
	 */
	private List<AddresseeReqDTO> addressee;

	/**
	 * 附件
	 */
	private List<EmailFileReqDTO> fileData;
}
