package com.tce.smart.app.vo.wechat;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加预约Vo
 *
 * @author mingkai.wu
 * @date 2019-05-13 10:18:09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckHostVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8881943069979050187L;

	/**
	 * 来访预约Id
	 */
	private String employeeId;

}
