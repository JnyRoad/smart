package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取招聘岗位列表Ao
 *
 * @author mingkai.wu
 * @date 2019-05-13 08:28:54
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobListAO extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1L;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 当前页
	 */
	private Integer current = 0;
	/**
	 * 每页数量
	 */
	private Integer size = 20;

}
