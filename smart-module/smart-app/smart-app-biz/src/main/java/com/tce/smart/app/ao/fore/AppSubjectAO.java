package com.tce.smart.app.ao.fore;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.ao.BaseAO;
import io.swagger.models.auth.In;
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
public class AppSubjectAO extends BaseAO {
	private static final long serialVersionUID = 6651993869362438552L;

	/**
	 * 记录Id
	 */
	private Integer id;

	/**
	 * 文章标题
	 */
	private String subTitle;

	/**
	 * 封面图片地址
	 */
	private String frontImg;

	/**
	 * 内容标识
	 */
	private Integer contentTextId;

	/**
	 * 添加日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date createTime;

}
