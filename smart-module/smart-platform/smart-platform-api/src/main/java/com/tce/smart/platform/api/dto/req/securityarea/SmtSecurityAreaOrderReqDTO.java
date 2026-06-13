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
 * @description: 保密区预约DTO
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtSecurityAreaOrderReqDTO implements Serializable {
	private static final long serialVersionUID = 5741137108419417584L;

	/**
	 * 到访区域
	 */
	@ApiModelProperty(value = "到访区域",required = true)
	private String visitArea;

	/**
	 * 来访事由
	 */
	@ApiModelProperty(value = "来访事由",required = true)
	private String visitType;

	/**
	 * 来访日期
	 */
	@ApiModelProperty(value = "来访日期",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date comeTime;

	/**
	 * 离开日期
	 */
	@ApiModelProperty(value = "离开日期",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date leaveTime;

	/**
	 * 供应商标识
	 */
	@ApiModelProperty(value = "供应商标识",required = true)
	private Long supplierId;

	/**
	 * 受访者名称
	 */
	@ApiModelProperty(value = "受访者名称",required = true)
	private String interviewName;

	/**
	 * 受访者电话
	 */
	@ApiModelProperty(value = "受访者电话",required = true)
	private String interviewPhone;

	/**
	 * 陪同者名称
	 */
	@ApiModelProperty(value = "陪同者名称",required = true)
	private String escortName;

	/**
	 * 陪同者电话
	 */
	@ApiModelProperty(value = "陪同者电话",required = true)
	private String escortPhone;

	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注",required = false)
	private String remark;

	/**
	 * 携带物品 多个物品已、号分隔
	 */
	@ApiModelProperty(value = "携带物品 多个物品已、号分隔",required = false)
	private String carryGoods;

	/**
	 * 附件内容 base64编码
	 */
	@ApiModelProperty(value = "附件内容-base64编码",required = false)
	private String additionalContent;


	/**
	 * 来访人员列表
	 */
	private List<SmtVisitListReqDTO> visitListReqDTOS;
}
