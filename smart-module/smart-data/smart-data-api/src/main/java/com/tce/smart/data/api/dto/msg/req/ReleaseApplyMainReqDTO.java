package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 放行条申请 提交到OA的参数实体类
 * @date: 2020-07-30 14:55
 * @author: wuling
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReleaseApplyMainReqDTO extends MainBaseTableReqDTO<ReleaseApplyMainReqDTO>{
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8728705920073413890L;

	/**
	 * 放行人级别
	 */
	private String sqrjb;

	/**
	 * 附件上传
	 */
	private String fjsc;

	/**
	 * 安检附件上传
	 */
	private String ajfjsc;

	/**
	 * 出发地点
	 */
	private String fxdd;

	/**
	 * 到达地点
	 */
	private String dddd;

	/**
	 * 出发地点详情
	 */
	private String fxddxq;

	/**
	 * 到达地点详情
	 */
	private String ddddxq;

	/**
	 * 物品放行类别
	 */
	private String wpfxlb;

	/**
	 * 是否返厂
	 */
	private String sffc;

	/**
	 * 流程编号
	 */
	private String lcbh;

	/**
	 * 申请人
	 */
	private String sqr;

	/**
	 * 申请部门
	 */
	private String sqbm;

	/**
	 * 放行事项
	 */
	private String fxsx;

	/**
	 * 人员放行
	 */
	private String ryfx;

	/**
	 * 物品放行
	 */
	private String wpfx;

	/**
	 * 放行去处
	 */
	private String fxqc;

	/**
	 * 放行事项
	 */
	private String fxsx1;

}
