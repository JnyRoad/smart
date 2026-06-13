package com.tce.smart.platform.api.dto.req.lock;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author sunfujian
 * @since 2021/10/25 15:08
 */
@Data
public class ApiDeviceAuthReqDTO extends BaseDTO {
	/**
	 * 每页显示条数，默认 10
	 */
	@NotNull(message = "分页大小不能为空")
	private long size;

	/**
	 * 当前页
	 */
	@NotNull(message = "当前页不能为空")
	private long current;
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    /**
     * 园区ID
     */
    @ApiModelProperty(value = "园区ID")
    private List<Integer> parkIds;
    private Integer parkId;
    /**
     * 人员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "人员ID")
    private Long personId;
    /**
     * 人员编号
     */
    @ApiModelProperty(value = "人员编号")
    private String personNum;
    /**
     * 人员名称
     */
    @ApiModelProperty(value = "人员名称")
    private String personName;
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String personPhone;
    /**
     * 设备ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("设备ID")
    private Long deviceId;
    /**
     * 设备唯一标识
     */
    @ApiModelProperty("设备唯一标识")
    private String deviceNum;
    /**
     * 设备名称
     */
    @ApiModelProperty("设备名称")
    private String deviceName;
    /**
     * 设备型号
     */
    @ApiModelProperty("设备型号")
    private String deviceType;
    /**
     * 设备位置
     */
    @ApiModelProperty("设备位置")
    private String deviceArea;

    @ApiModelProperty(value = "房间ID")
    private Long roomId;
    /**
     * 连接状态, 0:断开，1：连接，2：未激活
     */
    @ApiModelProperty(value = "连接状态 0:断开，1：连接，2：未激活")
    private Integer connectStatus;
    /**
     * 授权状态,0-待激活1-已激活2-激活失败3-已失效
     */
    @ApiModelProperty(value = "授权状态 0-待激活,1-已激活,2-激活失败,3-已失效")
    private Integer status;

    @ApiModelProperty(value = "同步标识，0：非同步，1：同步")
    private Integer syncFlag;
    /**
     * 有效开始时间
     */
    @ApiModelProperty("有效开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTimeStart;
    /**
     * 有效结束时间
     */
    @ApiModelProperty("有效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTimeEnd;
    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createUser;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateUser;
    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
