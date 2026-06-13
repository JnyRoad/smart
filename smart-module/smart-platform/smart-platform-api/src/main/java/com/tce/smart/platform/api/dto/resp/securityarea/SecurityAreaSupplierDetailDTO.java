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
 * @description: 保密区供应商详情
 * @date: 2020-07-31 9:13
 * @author: wuling
 * @version: 1.0
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaSupplierDetailDTO extends SecurityAreaSupplierDTO {

	private static final long serialVersionUID = -5543223473147809415L;

	@ApiModelProperty("协议签订时间")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date beginEffectTime;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("联系人")
	private String contactPerson;

	@ApiModelProperty(value = "协议类型 1.图片 2.PDF")
	private Integer protocolType;

	@ApiModelProperty(value = "协议名称")
	private String protocolName;

	@ApiModelProperty(value = "协议内容 图片地址或PDF下载地址")
	private List<String> contents;

	@ApiModelProperty(value = "授权列表")
	private String authorList;

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
