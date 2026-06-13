package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: SmtStaffStatementReqDTO
 * @date: 2020-07-17 17:59
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtStaffStatementReqDTO implements Serializable {
	private static final long serialVersionUID = -7289022427725195102L;

	/**
	 * 员工水电结算表标识
	 */
	private Long id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * BUID
	 */
	private Integer compId;

	/**
	 * 楼栋Id
	 */
	private Integer dormitoryId;

	/**
	 * 楼层Id
	 */
	private Integer floorId;

	/**
	 * 楼层Id
	 */
	private Integer roomId;

	/**
	 * 部门ID
	 */
	private Integer depId;


	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 结算月份
	 */
	@JsonFormat(pattern="yyyy-MM")
	@DateTimeFormat(pattern="yyyy-MM")
	private Date meterMonth;
}
