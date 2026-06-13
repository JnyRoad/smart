package com.tce.smart.app.vo.wechat;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 信息完善-身份证照片识别结果Vo
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:10:57
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PerfectInfoVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1192313527719429774L;

	/**
	 * 信息采集编号
	 */
	private Integer perfectId;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 身份证号
	 */
	private String identification;

}
