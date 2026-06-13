package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

import java.io.Serializable;

/**
 *邮箱收件人
 */
@Data
public class AddresseeReqDTO implements Serializable {


	private static final long serialVersionUID = 8001674565425115982L;

	/**
	 *
	 * 收件人
	 */
	private String username;

	/**
	 * 收件箱
	 */
	private String inbox;
}
