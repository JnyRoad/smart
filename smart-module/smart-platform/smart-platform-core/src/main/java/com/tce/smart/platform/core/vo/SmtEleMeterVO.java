package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sunfujian
 * @since 2021/11/3 18:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtEleMeterVO extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 电表ID
	 */
	private Long id;
	/**
	 * 电表序号
	 */
	private Integer seq;
	/**
	 * 电表名称
	 */
	private String name;
	/**
	 * 电表集中器ID
	 */
	private Long concentratorId;
	/**
	 * 电表集中器IP
	 */
	private String ip;
	/**
	 * 电表集中器端口号
	 */
	private Integer port;
	/**
	 * 园区ID
	 */
	private Integer parkId;
}
