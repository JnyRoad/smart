package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffAppealDTO
 * @date: 2020-07-23 14:52
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtStaffAppealReqDTO implements Serializable {
	private static final long serialVersionUID = 4570885826192242697L;

	/**
	 * 园区Id
	 */
	@ApiModelProperty(value = "园区Id",required = true)
	private Integer parkId;

	/**
	 * 申诉类型
	 */
	@ApiModelProperty(value = "申诉类型 1.人事服务 2.宿舍服务 3.车间管理",required = true)
	private Integer appealType;

	/**
	 * 发生时间
	 */
	@ApiModelProperty(value = "发生时间",required = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date happenTime;

	/**
	 * 文字描述
	 */
	@ApiModelProperty(value = "文字描述",required = false)
	private String appealDesc;

	/**
	 * 图片描述列表
	 */
	@ApiModelProperty(value = "图片列表",required = false)
	private List<String> appealImgList;
}
