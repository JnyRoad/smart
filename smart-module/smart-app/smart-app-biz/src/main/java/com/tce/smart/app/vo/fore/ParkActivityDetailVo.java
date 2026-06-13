package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区活动详情Vo
 *
 * @author mckaywu
 * @date 2019-06-18 09:42:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkActivityDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8075415710002292497L;

	/**
	 * 主键ID
	 */
	private Integer activityId;
	/**
	 * 标题
	 */
	private String activityTitle;
	/**
	 * 标题名称
	 */
	private String titleImage;
	/**
	 * 内容
	 */
	private String activityContent;
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
