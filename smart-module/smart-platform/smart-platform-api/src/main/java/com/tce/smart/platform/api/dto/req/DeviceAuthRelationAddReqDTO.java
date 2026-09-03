package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author sunfujian
 * @since 2021/9/2 10:13
 */
@Data
public class DeviceAuthRelationAddReqDTO extends BaseDTO {
	@NotNull(message = "授权ID不能为空")
	private Integer authId;

	@NotNull(message = "授权类型不能为空")
	private Integer type;

	/**
	 * 待授权的员工工号列表，来自 HTTP 外部输入。
	 * 上限取 Oracle IN 子句单批上限 1000：此前无上限时超 1000 必然 ORA-01795 报 500，
	 * 从未有客户端能成功提交更大的批次，收紧到 1000 不会破坏任何现有可用场景；
	 * 同时防住无界输入带来的内存与设备任务量放大（每个工号 × 关联设备数 都会生成下发任务）。
	 */
	@NotEmpty(message = "工号列表不能为空")
	@Size(max = 1000, message = "单次批量授权最多 1000 个工号")
	private List<String> badges;

	/**
	 * 权限开始日期，格式为 yyyy-MM-dd；未传时服务端默认当天。
	 */
	private String startTime;

	/**
	 * 权限结束日期，格式为 yyyy-MM-dd；未传时服务端默认 2030-12-31。
	 */
	private String endTime;
}
