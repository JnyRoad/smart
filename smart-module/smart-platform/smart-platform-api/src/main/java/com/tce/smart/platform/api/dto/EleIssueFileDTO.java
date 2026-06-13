package com.tce.smart.platform.api.dto;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/3/14 9:20
 */
@Data
public class EleIssueFileDTO extends BaseDTO {
	/**
	 * 设备IP【必选】
	 */
	private String deviceIp;

	/**
	 * 设备端口【必选】
	 */
	private Integer devicePort;
	/**
	 * 电表数量
	 */
	private Integer meterNum;
	/**
	 * 集中器通信地址
	 */
	private String concentratorAddress;
	/**
	 * json字符串存储电表信息：表序号，表通信地址
	 */
	private String meterJson;
}
