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
public class SmtWaterMeterVO extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 水表ID
	 */
	private Long id;
	/**
	 * 水表序号
	 */
	private Integer seq;
	/**
	 * 水表名称
	 */
	private String name;
	/**
	 * 水表集中器ID
	 */
	private Long concentratorId;
	/**
	 * 水表集中器IP
	 */
	private String ip;
	/**
	 * 水表集中器端口号
	 */
	private Integer port;
	/**
	 * 园区ID
	 */
	private Integer parkId;
}
