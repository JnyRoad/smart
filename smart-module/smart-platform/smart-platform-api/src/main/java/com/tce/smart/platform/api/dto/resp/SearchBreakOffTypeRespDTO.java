package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 调休类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchBreakOffTypeRespDTO implements Serializable {
	private static final long serialVersionUID = 873104555775679344L;

	/**
	 * 类型编号
	 */
	private String restCode;
	/**
	 * 类型名称
	 */
	private String restName;


}
