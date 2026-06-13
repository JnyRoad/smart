package com.tce.smart.platform.core.dto.securityarea;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: SecurityAreaOrderListDTO
 * @date: 2020-07-30 9:39
 * @author: wuling
 * @version: 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaOrderPartDTO implements Serializable {
	private static final long serialVersionUID = 1525020841426790019L;

	private Long id;

	/**
	 * 到访区域
	 */
	private String visitArea;

	/**
	 * 来访日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date comeTime;

	/**
	 * 离开日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date leaveTime;

	/**
	 * 状态
	 */
	private Integer status;
}
