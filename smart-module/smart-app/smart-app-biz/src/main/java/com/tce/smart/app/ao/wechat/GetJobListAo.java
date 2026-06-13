package com.tce.smart.app.ao.wechat;

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
public class GetJobListAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6835398161260308614L;

	/**
	 * 园区Id
	 */
	private String parkId;

}
