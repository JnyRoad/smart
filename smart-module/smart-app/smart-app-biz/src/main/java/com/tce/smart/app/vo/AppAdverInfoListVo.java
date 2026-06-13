package com.tce.smart.app.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/***
 * description: App广告保存Ao <br>
 * date: 2019/12/30 17:45 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
public class AppAdverInfoListVo extends BaseVO {
	private static final long serialVersionUID = 6580788557128526749L;

	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 图片
	 */
	private String image;

	/**
	 * 投放位置
	 */
	private String imagePosition;

	/**
	 * 跳转链接
	 */
	private String imageLink;

	/**
	 * 连接类型
	 */
	private String linkType;

	/**
	 * 发布状态（0:待发布；1:已发布；2:已下线））
	 */
	private String publishFlag;

}
