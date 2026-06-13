package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * @author fushiping
 * @date 2019/5/22 14:21
 **/
@Data
public class HomeBannerVo extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键ID
	 */
	private Integer pictureId;
	/**
	 * 链接URL
	 */
	private String linkUrl;
	/**
	 * 内容类型
	 */
	private Integer contentLinkType;
	/**
	 * 主题名称
	 */
	private String pictureName;
	/**
	 * 主题链接
	 */
	private String pictureUrl;

}
