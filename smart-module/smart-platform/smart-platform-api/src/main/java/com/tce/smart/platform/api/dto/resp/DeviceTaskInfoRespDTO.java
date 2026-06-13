package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备任务信息表
 *
 * @author
 * @date 2019-04-15 15:09:27
 */
@Data
public class DeviceTaskInfoRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;



	private String actionDesc;

	/**
	 * 任务状态：
	 * 0：初始化
	 * 1：成功
	 * 2：失败
	 * 3：处理中
	 * 4：已取消
	 */
	private String statusDesc;

	private Integer status;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 工号
	 */
	private String badge;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 设备名
	 */
	private String deviceName;

	/**
	 * 区域名
	 */
	private String areaName;


}
