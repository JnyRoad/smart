package com.tce.smart.platform.api.dto.req.remoteLock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人员入住/换宿/离宿数据
 * @author sunfujian
 * @date 2021/5/25 18:40
 */
@Data
public class LockDormitoryStaffDTO {
    /**
     * 楼栋ID
     */
    @ApiModelProperty(value = "楼栋ID")
    private Integer dormitoryId;
    /**
     * 楼层ID
     */
    @ApiModelProperty(value = "楼层ID")
    private Integer floorId;
    /**
     * 房间ID
     */
    @ApiModelProperty(value = "房间ID")
    private Integer roomId;
    /**
     * 旧房间ID，换宿时使用
     */
    @ApiModelProperty(value = "旧房间ID，换宿时使用")
    private Integer oldRoomId;
    /**
     * 入住时间
     */
    @ApiModelProperty(value = "入住时间")
    private LocalDateTime createTime;
    /**
     * 动作
     * A：入住
     * D：离宿
     * C：换宿
     */
    @ApiModelProperty(value = "动作,A：入住 D：离宿 C：换宿")
    private String action;

	/**
	 * 园区ID
	 */
	@ApiModelProperty(value = "园区ID")
	private Integer parkId;
	/**
	 * 员工工号
	 */
	@ApiModelProperty(value = "员工工号")
	private String badge;
	/**
	 * 员工姓名
	 */
	@ApiModelProperty(value = "员工姓名")
	private String name;
	/**
	 * 手机号
	 */
	@ApiModelProperty(value = "手机号")
	private String phone;

	/**
	 * 是否是员工 0-否  1-是
	 */
	@ApiModelProperty(value = "是否是员工")
	private Integer isStaff;
}
