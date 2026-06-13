package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 许昌车辆记录查询
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
public class XcVehicleDTO{

	private static final long serialVersionUID = 1L;

	/**
	 * 车牌号
	 */
	private String vehiclePlate;

	/**
	 * 车牌状态
	 */
	private Integer cardState;

	/**
	 * 开始时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;
}
