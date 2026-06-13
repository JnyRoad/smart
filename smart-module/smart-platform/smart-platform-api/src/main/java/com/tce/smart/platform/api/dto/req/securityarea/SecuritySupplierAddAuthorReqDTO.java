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
import java.util.List;

/**
 * @description: 保密区供应商添加授权项目DTO
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecuritySupplierAddAuthorReqDTO implements Serializable {
	private static final long serialVersionUID = 6387933874447511356L;

	@ApiModelProperty(value = "标识列表")
	private List<String> ids;

	@ApiModelProperty(value = "授权项目列表 以'/'分隔")
	private String authorList;
}
