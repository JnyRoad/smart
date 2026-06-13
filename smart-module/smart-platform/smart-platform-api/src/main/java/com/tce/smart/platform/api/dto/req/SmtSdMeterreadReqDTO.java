package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: SmtSdTemplatesReqDTO
 * @date: 2020-07-07 11:49
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSdMeterreadReqDTO implements Serializable {
	private static final long serialVersionUID = -5078663374494528533L;

	/**
	 * 记录Id
	 */
	private Long Id;

	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 楼栋Id
	 */
	private Integer dormitoryId;

	/**
	 * 楼层Id
	 */
	private Integer floorId;

	/**
	 * 房间号
	 */
	private Integer roomName;

	/**
	 * 抄表月份
	 */
	@JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 结算状态
	 */
	private Integer statementStatus;

	/**
	 * 查询是否已结算
	 */
	private Boolean isStatement;
}
