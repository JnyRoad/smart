package com.tce.smart.platform.api.dto.req.lock;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sunfujian
 * @since 2021/10/25 14:23
 */
@Data
public class DevicePageReqDTO extends BaseDTO {
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
	private String deviceName;
	private Integer parkId;

	/**
	 * 是否连接 0.未连接 1.已连接
	 */
	private Integer connectStatus;

	private Integer isAvailable;
	private Integer devicePower;
	private List<Integer> parkIds;
}
