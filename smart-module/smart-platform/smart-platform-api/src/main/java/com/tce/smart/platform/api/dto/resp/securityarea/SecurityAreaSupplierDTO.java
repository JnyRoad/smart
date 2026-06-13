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
public class SecurityAreaSupplierDTO implements Serializable {

	private static final long serialVersionUID = -2818869729383371838L;

	@ApiModelProperty("记录id")
	private Long id;

	@ApiModelProperty("园区Id")
	private Integer parkId;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("供应商类型 1.A类 2.非A类")
	private Integer supplierType;

	@ApiModelProperty("供应商代码")
	private String companyCode;

	@ApiModelProperty("供应商名称")
	private String companyName;

	@ApiModelProperty("协议到期时间")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date endEffectTime;

	@ApiModelProperty("是否存在协议 0.不存在 1.存在")
	private Integer hasProtocol;

	@ApiModelProperty(value = "携带物品列表")
	private List<String> itemList;

	@ApiModelProperty(value = "授权区域")
	private String authorizedArea;

	@ApiModelProperty(value = "授权人数")
	private Integer authPersonNum;

	@ApiModelProperty(value = "身份证号")
	private String certNo;

	@ApiModelProperty(value = "姓名")
	private String personName;
}
