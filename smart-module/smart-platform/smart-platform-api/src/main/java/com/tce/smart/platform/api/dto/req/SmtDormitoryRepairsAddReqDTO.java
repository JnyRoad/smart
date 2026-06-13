package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 宿舍报修请求实体类
 * @date: 2020-07-20 14:13
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtDormitoryRepairsAddReqDTO implements Serializable {
	private static final long serialVersionUID = -7289022427725195102L;
	/**
	 * 园区Id
	 */
	@ApiModelProperty(value = "园区Id",required = true)
	private Integer parkId;

	/**
	 * 楼栋
	 */
	@ApiModelProperty(value = "楼栋",required = true)
	private String dormitoryName;

	/**
	 * 房间名称
	 */
	@ApiModelProperty(value = "房间名称",required = true)
	private String roomName;
	/**
	 * 范围类型
	 */
	@ApiModelProperty(value = "范围类型",required = true)
	private Integer rangeType;
	/**
	 * 维修类型
	 */
	@ApiModelProperty(value = "维修类型 1.灯 2.插座 3.水龙头 4.水管 5.门 6.锁 7.空调",required = true)
	private Integer repairType;

	/**
	 * 故障描述
	 */
	@ApiModelProperty(value = "故障描述",required = false)
	private String faultDesc;

	/**
	 * 故障图片
	 */
	@ApiModelProperty(value = "故障图片",required = false)
	private List<String> faultImgs;

}
