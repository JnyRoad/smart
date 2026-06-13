package com.tce.smart.platform.core.dto.commonsd;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: 公摊水电表记录DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class CommonSDRecordDTO {
	/**
	 * 记录ID
	 */
	private Long id;

	/**
	 * 水电表名称
	 */
	private String sdName;

	/**
	 * 收费项目 1.热水 2.冷水 3.电
	 */
	private Integer categoryId;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;


	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;

	/**
	 * 房间列表
	 */
	private String roomIdList;
}
