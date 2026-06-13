package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: 员工查询园区报修记录实体类
 * @date: 2020-07-20 14:23
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtDormitoryRepairsRespVO {
	/**
	 * 记录Id
	 */
	@ApiModelProperty("记录Id")
	private Long id;

	/**
	 * 报修人工号
	 */
	@ApiModelProperty("报修人工号")
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
	 * 范围类型
	 */
	private Integer rangeType;
	/**
	 * 范围类型
	 */
	private String rangeTypeDesc;

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
	 * 报修时间
	 */
	@ApiModelProperty("报修时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 故障描述
	 */
	@ApiModelProperty("故障描述")
	private String faultDesc;
}
