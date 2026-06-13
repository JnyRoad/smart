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
 * @description: 批量上传保密区供应商DTO
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaSupplierUploadReqDTO implements Serializable {
	private static final long serialVersionUID = 6387933874447511356L;

	@ApiModelProperty(value = "园区Id",required = true)
	private Integer parkId;

	@ApiModelProperty(value = "供应商类型 1.A类 2.非A类",required = true)
	private Integer supplierType;

	@ApiModelProperty(value = "供应商信息列表")
	private List<SupplierInfo> supplierInfoList;

	/**
	 * 供应商信息
	 */
	@Data
	public static class SupplierInfo{
		@ApiModelProperty(value = "供应商名称",required = true)
		private String companyName;

		@ApiModelProperty(value = "协议生效日期",required = true)
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@JsonFormat(pattern="yyyy-MM-dd")
		private Date beginEffectTime;

		@ApiModelProperty(value = "协议到期日期",required = true)
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@JsonFormat(pattern="yyyy-MM-dd")
		private Date endEffectTime;

		@ApiModelProperty(value = "申请理由")
		private String remark;

		@ApiModelProperty(value = "联系人")
		private String contactPerson;

		@ApiModelProperty(value = "协议类型 1.图片 2.PDF")
		private Integer procotolType;

		@ApiModelProperty(value = "协议内容-base64编码")
		private String proContent;

		@ApiModelProperty(value = "授权项目列表")
		private String authorList;

		@ApiModelProperty(value = "授权区域")
		private String authorizedArea;

		@ApiModelProperty(value = "授权人数")
		private Integer authPersonNum;

		@ApiModelProperty(value = "身份证号")
		private String certNo;

		@ApiModelProperty(value = "姓名")
		private String personName;
	}
}
