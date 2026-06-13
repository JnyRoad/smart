package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 邮箱对象
 * @author: puao
 * @create: 2019-07-04 16:00
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5523705479741296578L;

	/**
	 * 邮件标题
	 */
	private String title;
	/**
	 * 邮件内容
	 * 支持html内容
	 */
	private String content;

	/**
	 * 收件人邮箱 支持批量发送
	 */
	private List<String> inboxs;

	/**
	 * 附件
	 */
	private List<EmailFileReqDTO> fileData;
}
