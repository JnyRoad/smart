package com.tce.smart.platform.api.dto.resp.securityarea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 保密区供应商
 * @date: 2020-07-31 9:13
 * @author: wuling
 * @version: 1.0
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaSupplierListDTO implements Serializable {

	private static final long serialVersionUID = -2818869729383371838L;

	@ApiModelProperty("园区Id")
	private Integer parkId;

	@ApiModelProperty("园区Name")
	private String parkName;

	@ApiModelProperty("保密区列表")
	List<SecurityAreaSupplierDTO> children;

}
