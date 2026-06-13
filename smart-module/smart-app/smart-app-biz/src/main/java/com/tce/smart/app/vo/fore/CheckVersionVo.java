package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CheckVersionVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -6007530807251820529L;

	/**
	 * 是否需要升级
	 */
	private Boolean isNeedUpdate;

	/**
	 * 是否需要重新安装
	 */
	private Boolean isNeedReInstall;

	/**
	 * App最新版本号
	 */
	private String latestVersion;

	/**
	 * App最新版本升级地址
	 */
	private String patchUrl;

	/**
	 * App安装包地址
	 */
	private String installAppUrl;
}
