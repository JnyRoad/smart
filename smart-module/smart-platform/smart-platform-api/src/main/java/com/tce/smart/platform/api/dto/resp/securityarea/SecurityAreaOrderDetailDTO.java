package com.tce.smart.platform.api.dto.resp.securityarea;

import com.tce.smart.platform.api.dto.req.securityarea.SmtVisitListReqDTO;
import com.tce.smart.platform.api.dto.resp.FlowRespDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @description: 保密区预约详情实体类
 * @date: 2020-07-31 9:13
 * @author: wuling
 * @version: 1.0
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityAreaOrderDetailDTO extends SecurityAreaOrderListDTO{

	/**
	 * 申请部门
	 */
	@ApiModelProperty("申请部门")
	private String applyDep;

	/**
	 * 访问事由
	 */
	@ApiModelProperty("访问事由")
	private String visitType;

	/**
	 * 来访单位
	 */
	@ApiModelProperty("来访单位")
	private String supplier;

	/**
	 * 受访者名称
	 */
	@ApiModelProperty("受访者名称")
	private String interViewName;

	/**
	 * 受访者电话
	 */
	@ApiModelProperty("受访者电话")
	private String interViewPhone;

	/**
	 * 陪同者名称
	 */
	@ApiModelProperty("陪同者名称")
	private String escortName;

	/**
	 * 陪同者电话
	 */
	@ApiModelProperty("陪同者电话")
	private String escortPhone;

	/**
	 * 携带物品 多个物品已、号分隔
	 */
	@ApiModelProperty("携带物品 多个物品已、号分隔")
	private String carryGoods;

	/**
	 * 附件链接地址
	 */
	@ApiModelProperty("附件链接地址")
	private String additionalLink;

	/**
	 * 来访人员列表
	 */
	@ApiModelProperty("来访人员列表")
	private List<SmtVisitListReqDTO> visitListReqDTOS;

	@ApiModelProperty("OA审批列表")
	private List<FlowRespDTO> flowList;
}
