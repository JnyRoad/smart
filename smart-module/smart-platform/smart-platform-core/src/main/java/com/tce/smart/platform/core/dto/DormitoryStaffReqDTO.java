package com.tce.smart.platform.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 床位及占用员工信息
 *
 * @author QIPEI
 *
 */
@Data
public class DormitoryStaffReqDTO {


	/**
	 * 员工入住表主键
	 */
	private Integer id;
	/**
	 * 员工号
	 */
	@ApiModelProperty(name = "员工号", required = true)
	private String staffBadge;

	@ApiModelProperty(name = "员工性别", required = true)
	private String staffSex;

	/**
	 * 员工姓名
	 */
	@ApiModelProperty(name = "员工姓名", required = true)
	private String name;

	/**
	 * 入住员工在职状态 1-在职 0-离职
	 */
	private Integer status;

	/**
	 * 床位编号
	 */
	@ApiModelProperty(name = "床位编号", required = true)
	private Integer bedNumber;

	/**
	 * 床位名称
	 */
	private String bedName;

	/**
	 * 房间号
	 */
	@ApiModelProperty(name = "房间号", required = true)
	private String roomName;

	/**
	 * 默认是员工类型
	 */
	@ApiModelProperty(name = "房间分类", required = true)
	private String roomType;

	/**
	 * BU
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 宿舍类型
	 */
	private String dormitoryTypeName;

	/**
	 * 入住时间
	 */
	@ApiModelProperty(name = "入住时间", required = true)
	private Date createTime;

	private String jobName;

	/**
	 * 是否是员工0-非员工 1-员工
	 */
	private Integer isStaff;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 楼栋名称
	 */
	@ApiModelProperty(name = "楼栋名称", required = true)
	private String dormitoryName;

	/**
	 * 楼层号
	 */
	@ApiModelProperty(name = "楼层号", required = true)
	private Integer floorName;

	/**
	 * BU
	 */
	private String dorCompName;

	/**
	 * 部门名称
	 */
	private String dorDepName;

	/**
	 * 岗位名称
	 */
	private String dorJobName;

    /**
     * 失败记录信息
	 */
	private String mark;

}
