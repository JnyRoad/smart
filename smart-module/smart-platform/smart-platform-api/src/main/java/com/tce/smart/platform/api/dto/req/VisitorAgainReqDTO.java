package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 查询访问者预约记录DTO
 * @date: 2020-08-07 9:11
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorAgainReqDTO implements Serializable {
	private static final long serialVersionUID = 5002297691726091945L;

	/**
	 * 预约记录Id
	 */
	private Long id;

	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date startTime;

	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date endTime;
}
