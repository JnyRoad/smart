package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 引导欢迎页Vo
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:10:57
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WelcomeVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1192313527719429774L;

	/**
	 * 引导信息ID
	 */
	private String welcomeId;

	/**
	 * 引导标题
	 */
	private String welcomeTitle;

	/**
	 * 引导内容
	 */
	private String welcomeContent;

	/**
	 * 引导图片访问地址
	 */
	private String pictureUrl;

}
