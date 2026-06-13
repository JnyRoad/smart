package com.tce.smart.platform.api.dto.resp.dormitoryrepairs;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @description: 宿舍报修记录实体类
 * @date: 2020-07-20 14:23
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtDormitoryRepairsDTO {

	/**
	 * 记录Id
	 */
	@ApiModelProperty("记录Id")
	private Long id;

	/**
	 * 工号
	 */
	@ApiModelProperty("工号")
	private String staffBadge;

	/**
	 * 报修人姓名
	 */
	@ApiModelProperty("报修人姓名")
	private String name;

	/**
	 * BU名称
	 */
	@ApiModelProperty("BU名称")
	private String compName;

	/**
	 * 部门名称
	 */
	@ApiModelProperty("部门名称")
	private String depName;

	/**
	 * 范围类型
	 */
	@ApiModelProperty("范围类型")
	private Integer rangeType;

	/**
	 * 范围类型描述
	 */
	@ApiModelProperty("范围类型描述")
	private String rangeTypeDesc;


	/**
	 * 维修类型
	 */
	@ApiModelProperty("维修类型")
	private Integer repairType;

	/**
	 * 维修类型描述
	 */
	@ApiModelProperty("维修类型描述")
	private String repairTypeDesc;

	/**
	 * 楼栋名称
	 */
	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	/**
	 * 房间名称
	 */
	@ApiModelProperty("房间名称")
	private String roomName;

	/**
	 * 维修状态
	 */
	@ApiModelProperty("维修状态")
	private Integer status;

	/**
	 * 状态描述
	 */
	@ApiModelProperty("状态描述")
	private String statusDesc;

	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	private Date createTime;
}
