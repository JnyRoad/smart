package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区文化列表Vo
 *
 * @author mckaywu
 * @date 2019-06-18 09:44:07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkCultureListVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8509615612858622603L;

	/**
	 * 主键ID
	 */
	private Integer cultureId;
	/**
	 * 标题
	 */
	private String cultureTitle;
	/**
	 * 链接
	 */
	private String cultureUrl;
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
