package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 保密区预约申请 提交到OA的参数实体类
 * @date: 2020-07-30 14:55
 * @author: wuling
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityAreaVisitMainReqDTO extends MainBaseTableReqDTO<SecurityAreaVisitMainReqDTO>{
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8728705920073413890L;

	/**
	 * 流程编号
	 */
	private String liucbh;

	/**
	 * 来访单位
	 */
	private String laifdw;

	/**
	 * 来访区域
	 */
	private String laifqy;

	/**
	 * 来访日期
	 */
	private String laifrq;

	/**
	 * 携带物品
	 */
	private String xiedwf;

	/**
	 * 来访事由
	 */
	private String laifsy;

	/**
	 * 保密协议
	 */
	private Integer bmxy;

	/**
	 * 受访者
	 */
	private String sfz;

	/**
	 * 联系电话
	 */
	private Long lxdh;

	/**
	 * 陪同者
	 */
	private String ptz;

	/**
	 * 配同者联系电话
	 */
	private Long lxdh1;

	/**
	 * 备注
	 */
	private String beiz;

	/**
	 * 申请人
	 */
	private String shenqr;

	/**
	 * 是否加签
	 */
	private Integer shifjq;

	/**
	 * 申请人部门
	 */
	private String shenqrbm;

	/**
	 * 是否属于NPI区域
	 */
	private Integer sfsy;

	/**
	 * 联系电话1
	 */
	private String laixdh2;

	/**
	 * 联系电话1
	 */
	private String laixdh3;

	/**
	 * 来访时间
	 */
	private String laifsj;

	/**
	 * 离去日期
	 */
	private String lkrq;

	/**
	 * 离开时间
	 */
	private String lksj;

}
