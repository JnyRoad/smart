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
public class DormitoryQuickStaffRespDTO implements Serializable {
	private static final long serialVersionUID = 884363616374909240L;

	@ApiModelProperty("记录ID")
	private Integer id;

	@ApiModelProperty("员工名称")
	private String name;

	@ApiModelProperty("员工工号")
	private String staffBadge;

	@ApiModelProperty("性别 0.男 1.女")
	private Integer sex;

	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	@ApiModelProperty("房间名称")
	private Integer roomId;

	@ApiModelProperty("房间名称")
	private String roomName;

	@ApiModelProperty("床位编号")
	private Integer bedNumber;

	@ApiModelProperty(value = "部门名称")
	private String depName;

	@ApiModelProperty(value = "职级名称")
	private String dorJobName;

	@ApiModelProperty(value = "入住时间")
	private Date createTime;

	@ApiModelProperty(value = "是否当前入住记录 0 否 1 是")
	private Integer isFlag;

	@ApiModelProperty(value = "床位ID")
	private Integer bedId;
}
