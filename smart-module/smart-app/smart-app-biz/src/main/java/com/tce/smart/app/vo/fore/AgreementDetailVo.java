package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区信息VO
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgreementDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7776260457623907497L;

	/**
	 * 协议编号
	 */
	private String agreeId;

	/**
	 * 协议名称
	 */
	private String agreeName;

	/**
	 * 协议内容
	 */
	private String agreeContent;

}
