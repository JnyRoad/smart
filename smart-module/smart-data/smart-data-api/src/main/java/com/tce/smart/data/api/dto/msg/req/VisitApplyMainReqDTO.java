package com.tce.smart.data.api.dto.msg.req;

import lombok.*;

/**
 * 访客申请主表
 * @date: 2020-07-30 14:55
 * @author: fushiping
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VisitApplyMainReqDTO extends MainBaseTableReqDTO<VisitApplyMainReqDTO>{
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8728705920073413890L;

	/**
	 * 申请人姓名
	 */
	private String sqrxm;

	/**
	 * 申请人工号
	 */
	private String sqrgh;

	/**
	 * 申请部门
	 */
	private String sqrbm;

	/**
	 * 来访单位
	 */
	private String lfdw;

	/**
	 * 来访园区
	 */
	private String lfyq;

	/**
	 * 携带物品
	 */
	private String xdwp;

	/**
	 * 车牌号
	 */
	private String cph;

	/**
	 * 被访人
	 */
	private String bfr;

	/**
	 * 申请时间
	 */
	private String sqsj;


	/**
	 * 来访事由
	 */
	private String lfsy;


	/**
	 * 备注
	 */
	private String bz;


	/**
	 * 行程码
	 */
	private String xcm2;

	/**
	 * 当地健康码
	 */
	private String jkm2;

	/**
	 * 来访手机号码
	 */
	private String lfrsjhm;

	private String startTime;

	private String endTime;

	/**
	 * 福利层次
	 */
	private String flcj;

}
