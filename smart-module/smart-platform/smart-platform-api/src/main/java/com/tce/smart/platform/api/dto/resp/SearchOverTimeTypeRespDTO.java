package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 加班类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchOverTimeTypeRespDTO implements Serializable {
	private static final long serialVersionUID = 8192373481881572018L;

	/**
	 * 类型编号
	 */
	private String extraworkType;
	/**
	 * 类型名称
	 */
	private String extraworkTypeName;


}
