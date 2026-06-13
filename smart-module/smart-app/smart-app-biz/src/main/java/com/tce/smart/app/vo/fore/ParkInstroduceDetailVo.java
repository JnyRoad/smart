package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区简介详情Vo
 *
 * @author mckaywu
 * @date 2019-06-18 09:42:57
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkInstroduceDetailVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -4933307709499220795L;
	/**
	 * 主键ID
	 */
	private Integer instroduceId;
	/**
	 * 标题
	 */
	private String instroduceTitle;
	/**
	 * 标题名称
	 */
	private String titleImage;
	/**
	 * 内容
	 */
	private String instroduceContent;
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
