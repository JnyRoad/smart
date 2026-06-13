package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人脸找回密码校验结果Vo
 *
 * @author mkwu
 * @date 2019-07-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChackFacePwdVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6147191433585833311L;

	/**
	 * 员工号
	 */
	private String username;

	/**
	 * 授权码
	 */
	private String pwdUpdateAuthCode;

}
