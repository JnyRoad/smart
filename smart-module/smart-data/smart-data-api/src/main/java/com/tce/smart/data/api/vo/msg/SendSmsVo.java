package com.tce.smart.data.api.vo.msg;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信发送响应Vo
 *
 * @author qipei
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendSmsVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2150347699663942002L;

	/**
	 * 接口响应码
	 */
	private String result;

	/**
	 * 短信发送成功返回Id
	 */
	private String taskid;

	/**
	 * 描述
	 */
	private String description;

	private String faillist;

}
