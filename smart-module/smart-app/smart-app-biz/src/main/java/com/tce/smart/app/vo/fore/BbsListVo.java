package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class BbsListVo extends BaseVO {
	/**
	 * 主键ID
	 */
	private Integer bbsId;
	/**
	 * 主题名称
	 */
	private String bbsTitle;
	/**
	 * 主题图片
	 */
	private String bbsImg;

	/**
	 * 公告链接
	 */
	private String bbsUrl;
	/**
	 * 内容类型
	 */
	private Integer contentLinkType;
}
