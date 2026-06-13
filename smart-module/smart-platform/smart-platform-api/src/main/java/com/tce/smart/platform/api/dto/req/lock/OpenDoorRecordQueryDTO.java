package com.tce.smart.platform.api.dto.req.lock;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author sunfujian
 * @since 2021/9/26 9:01
 */
@Data
public class OpenDoorRecordQueryDTO extends BaseDTO {
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
	 * 园区ID
	 */
	@ApiModelProperty(value = "园区ID")
	private List<Integer> parkIds;
	private Integer parkId;
    /**
     * 设备名称
     */
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

	@ApiModelProperty(value = "人员编号")
	private String personNum;

	@ApiModelProperty(value = "人员姓名")
	private String personName;

	@ApiModelProperty(value = "开门方式 1.密码 2.指纹 3.卡片 4.远程开门")
	private Integer openType;
    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
