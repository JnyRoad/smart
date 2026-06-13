package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class NewsDetailVo extends BaseVO {
	/**
	 * 主键ID
	 */
	private Integer newsId;
	/**
	 * 标题图片
	 */
	private String titleImage;
	/**
	 * 主题名称
	 */
	private String newsTitle;
	/**
	 * 主题文本内容
	 */
	private String newsContent;
	/**
	 * 创建时间
	 */
	private LocalDateTime date;


	/**
	 * 附件名
	 */
	private String enclosureName;

	/**
	 * 附件下载URL
	 */
	private String enclosureUrl;

	/**
	 * PDF附件预览地址
	 */
	private String previewUrl;


}
