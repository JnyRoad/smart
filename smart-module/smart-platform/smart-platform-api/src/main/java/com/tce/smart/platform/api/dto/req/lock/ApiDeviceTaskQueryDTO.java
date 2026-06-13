package com.tce.smart.platform.api.dto.req.lock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author sunfujian
 * @date 2021/8/24 9:15
 */
@Data
public class ApiDeviceTaskQueryDTO {
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
    private String personName;
    private String personPhone;
    private String personNum;
    private String deviceName;
    private Integer status;
    private Long deviceId;
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
