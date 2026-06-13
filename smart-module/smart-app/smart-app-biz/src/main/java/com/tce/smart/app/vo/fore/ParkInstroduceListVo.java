package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区简介列表Vo
 *
 * @author mckaywu
 * @date 2019-06-18 09:43:16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkInstroduceListVo extends BaseVO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 3455197705469375459L;

	/**
	 * 主键ID
	 */
	private Integer instroduceId;
	/**
	 * 标题
	 */
	private String instroduceTitle;
	/**
	 * 链接
	 */
	private String instroduceUrl;
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
