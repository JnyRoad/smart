package com.tce.smart.platform.api.dto.req.securityarea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 保密区供应商协议过期通知状态
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupplierNotifyStatusReqDTO implements Serializable {
	private static final long serialVersionUID = 8682183760580060110L;

	@ApiModelProperty(value = "记录标识",required = true)
	private Long id;

	@ApiModelProperty(value = "通知状态 0.未通知 1.已通知",required = true)
	private Integer notifyStatus;
}
