package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.dto.securityarea.SecurityAreaOrderPartDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @description: 人员抓拍统计数据
 * @date: 2020-08-05 17:24
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SnapPersonStatisDTO {

	/**
	 * 位置编号
	 */
	private Integer areaId;

	/**
	 * 位置
	 */
	private String areaName;

	/**
	 * 数量
	 */
	private Integer count;

	/**
	 * 进出类型
	 */
	private Integer eventType;
}
