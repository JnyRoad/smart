package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区概况列表Vo
 *
 * @author mckaywu
 * @date 2019-06-18 09:43:46
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkGeneralListVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6753893125716775579L;

	/**
	 * 主键ID
	 */
	private Integer generalId;
	/**
	 * 标题
	 */
	private String generalTitle;
	/**
	 * 链接
	 */
	private String generalUrl;
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
