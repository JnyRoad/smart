package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class NewsListVo extends BaseVO {
	/**
	 * 主键ID
	 */
	private Integer newsId;
	/**
	 * 主题名称
	 */
	private String newsTitle;
	/**
	 * 主题链接
	 */
	private String newsUrl;
	/**
	 * 标题图片
	 */
	private String titleImage;
	/**
	 * 内容链接类型
	 */
	private Integer contentLinkType;
	/**
	 * 创建时间
	 */
	private LocalDateTime date;
}
