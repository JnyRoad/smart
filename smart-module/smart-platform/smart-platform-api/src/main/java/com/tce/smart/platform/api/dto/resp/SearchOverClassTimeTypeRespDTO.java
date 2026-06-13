package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 班别类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchOverClassTimeTypeRespDTO implements Serializable {
	private static final long serialVersionUID = 4985999442642356601L;

	/**
	 * 类型编号
	 */
	private String extraworkClassCode;
	/**
	 * 类型名称
	 */
	private String restName;


}
