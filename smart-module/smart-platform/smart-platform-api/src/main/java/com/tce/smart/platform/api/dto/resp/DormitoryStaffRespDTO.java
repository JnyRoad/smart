package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: DormitoryStaffRespDTO
 * @date: 2020/9/28 18:12
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryStaffRespDTO implements Serializable {
	private static final long serialVersionUID = 451937157597507433L;

	@ApiModelProperty("记录ID")
	private Integer id;

	@ApiModelProperty("员工名称")
	private String staffName;

	@ApiModelProperty("员工工号")
	private String staffBadge;

	@ApiModelProperty("性别 0.男 1.女")
	private Integer sex;

	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	@ApiModelProperty("房间名称")
	private Integer roomId;

	@ApiModelProperty("房间名称")
	private Integer roomName;

	@ApiModelProperty("床位编号")
	private Integer bedNum;

	@ApiModelProperty(value = "部门名称")
	private String depName;

	@ApiModelProperty(value = "职级名称")
	private String jobName;

	@ApiModelProperty(value = "入住时间")
	private Date createTime;
}
