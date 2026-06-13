package com.tce.smart.data.api.dto.msg.req;

import lombok.*;

/**
 * 入厂申请 提交到OA的参数实体类
 * @date: 2020-07-30 14:55
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EntryFactoryApplyMainReqDTO extends MainBaseTableReqDTO<EntryFactoryApplyMainReqDTO>{
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8728705920073413890L;

	/**
	 * 授权进入区域类别
	 */
	private String sqjrqy1;

	/**
	 * 申请人
	 */
	private String sqr;

	/**
	 * 申请部门
	 */
	private String sqbm;

	/**
	 * 申请时间
	 */
	private String sqsj;

	/**
	 * 来访单位
	 */
	private String lfdw;

	/**
	 * 来访事由
	 */
	private String lfsy;

	/**
	 * 授权进入区域
	 */
	private String sqjrqy;

	/**
	 * 是否拍照 是=0，否=1
	 */
	private String sfpz;

	/**
	 * 参观机台 是=0，否=1
	 */
	private String cgjt;

	/**
	 * 携带物品
	 */
	private String xdwp;

	/**
	 * 区域接待人
	 */
	private String qyjdr;

	/**
	 * 流程编号
	 */
	private String lcbh;

	/**
	 * 监督保安
	 */
	private String jdba;

	/**
	 * 短期来访
	 */
	private String dqlf;

	/**
	 * 来访类别
	 */
	private String lflb;

	/**
	 * 长期来访
	 */
	private String cqlf;

	/**
	 * 车辆通行证办理
	 */
	private String cltxz;

	/**
	 * 批量附件
	 */
	private String plfj;

	/**
	 * 入厂状态
	 */
	private String rczt;

	/**
	 * 来访日期
	 */
	private String lfsj;

	/**
	 * 授权进入区域
	 */
	private String sqjrqynew;

	/**
	 * 区域
	 */
	private String qy;

	/**
	 * 携带物品
	 */
	private String xdwpnew;

	/**
	 * 来访种类
	 */
	private String lfzl;

	/**
	 * 授权进入区域
	 */
	private String sqjrqytxt;

	/**
	 * 是否进入车间
	 */
	private String sfjrcj;

	private String a;

	private String b;

	private String c;

	private String d;

	private String e;

	private String f;

	private String g;

	private String h;

	private String i;

	private String gg;

	private String k;

	private String l;

	private String m;

	/**
	 * 新厂区
	 */
	private String aaa;

	private String bbb;

}
