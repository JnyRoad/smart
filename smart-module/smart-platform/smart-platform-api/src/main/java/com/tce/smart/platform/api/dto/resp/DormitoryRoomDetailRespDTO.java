package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryLockInfoRespDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: DormitoryRoomDetailRespDTO
 * @date: 2020/9/28 18:12
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryRoomDetailRespDTO implements Serializable {
	private static final long serialVersionUID = 6674993102031601865L;

	@ApiModelProperty("床位编号")
	private Integer id;

	@ApiModelProperty("床位名称")
	private String bedNumber;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("楼栋Id")
	private Integer dormitoryId;

	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	@ApiModelProperty("楼层Id")
	private Integer floorId;

	@ApiModelProperty("楼层名称")
	private String floorName;

	@ApiModelProperty("房间Id")
	private Integer roomId;

	@ApiModelProperty("房间名称")
	private String roomName;

	@ApiModelProperty("入住记录ID")
	private Integer inRecordId;

	@ApiModelProperty("入住人工号")
	private String staffBadge;

	@ApiModelProperty("入住人名称")
	private String staffName;

	@ApiModelProperty("入住时间")
	private Date inDate;

	@ApiModelProperty("在职情况 0.离职 1.在职 null.未入职")
	private Integer status;

	@ApiModelProperty("性别 0.男 1.女")
	private Integer sex;

	@ApiModelProperty("部门名称")
	private String depName;

	@ApiModelProperty("职位名称")
	private String jobName;

	@ApiModelProperty("是否删除 1.已删除 0.未删除")
	private Integer delFlag;

	@ApiModelProperty("入住详情")
	private String detailStr;

	@ApiModelProperty("门锁动态开门密码")
	private DormitoryLockInfoRespDTO lockPwd;
}
