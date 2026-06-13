package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区活动
 * @author mckaywu
 * @date 2019-06-18 09:44:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkActivityListVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7264848066769979738L;

	/**
	 * 主键ID
	 */
	private Integer activityId;
	/**
	 * 标题
	 */
	private String activityTitle;
	/**
	 * 链接
	 */
	private String activityUrl;
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
