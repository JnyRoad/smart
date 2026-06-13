package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: SmtSecurityAreaSupplierReqDTO
 * @date: 2020-07-22 17:58
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSecurityAreaSupplierReqDTO implements Serializable {
	private static final long serialVersionUID = -1100584413703267662L;

	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 单位名称
	 */
	private String companyName;

	/**
	 * 开始生效时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date beginEffectTime;

	/**
	 * 结束生效时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date endEffectTime;
}
