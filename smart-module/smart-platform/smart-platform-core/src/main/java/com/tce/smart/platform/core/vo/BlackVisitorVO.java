package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;
/**
 * 黑名单访客
 * @author QIPEI
 *
 */
@Data
public class BlackVisitorVO {

private Integer id;

	/**
	 * 身份证号
	 */
	private String cardNo;

	/**
	 * 黑名单姓名
	 */
	private String personName;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 园区
	 */
	private String parkName;

	private String createUser;

	private String reason;
}
