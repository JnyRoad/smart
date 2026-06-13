package com.tce.smart.app.ao.fore;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.ao.BaseAO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;

/**
 * @description: app查询申诉专区文章列表响应
 * @date: 2020-07-28 18:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class AppSubjectDetailAO extends BaseAO {
	private static final long serialVersionUID = 103620784443013558L;

	/**
     * 记录Id
	 */
	@ApiModelProperty("记录Id")
	private Integer id;

	/**
	 * 内容类型
	 */
	@ApiModelProperty("内容类型: 1.链接 2.文本内容 3.PDF")
	private Integer type;

	/**
	 * 文本内容
	 */
	@ApiModelProperty("内容")
	private String contentText;

}
