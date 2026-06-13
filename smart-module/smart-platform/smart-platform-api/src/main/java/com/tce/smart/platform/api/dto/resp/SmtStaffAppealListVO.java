package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;

/**
 * @description: 员工申诉列表
 * @date: 2020-07-23 17:05
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtStaffAppealListVO {

	/**
	 * 标识Id
	 */
	@ApiModelProperty("记录Id")
	private Long id;

	/**
	 * 员工工号
	 */
	@ApiModelProperty("员工工号")
	private String badge;

	/**
	 * 员工名称
	 */
	@ApiModelProperty("员工名称")
	private String staffName;

	/**
	 * 申诉类型
	 */
	@ApiModelProperty("申诉类型")
	private Integer appealType;

	/**
	 * 申诉类型描述
	 */
	@ApiModelProperty("申诉类型描述")
	private String appealTypeDesc;

	/**
	 * 状态
	 */
	@ApiModelProperty("状态")
	private Integer status;

	/**
	 * 状态描述
	 */
	@ApiModelProperty("状态描述")
	private String statusDesc;

	/**
	 * 反馈时间
	 */
	@ApiModelProperty("创建时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 反馈描述
	 */
	@ApiModelProperty("反馈描述")
	private String appealDesc;

	/**
	 * 图片描述列表
	 */
	@ApiModelProperty("图片描述列表-链接地址")
	private List<String> appealImgs;

	/**
	 * 是否转交Ta人
	 */
	@ApiModelProperty(hidden = true)
	private Boolean isChange;
}
