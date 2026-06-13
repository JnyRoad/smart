package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮件附件
 * @author: puao
 * @create: 2019-09-25 14:23
 **/
@Data
public class EmailFileReqDTO implements Serializable {

	private static final long serialVersionUID = -425812011528127455L;

	/**
	 * 文件名
	 *  ps:带上文件后缀
	 */
	private String fileName;

	/**
	 * 文件数据
	 */
	private byte[] fileBytes;
}
