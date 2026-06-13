package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 保密权限申请 提交到OA的参数实体类
 * @date: 2020-07-30 14:55
 * @author: wuling
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityAuthApplyMainReqDTO extends MainBaseTableReqDTO<SecurityAuthApplyMainReqDTO>{
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8728705920073413890L;

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
	 * 申请项目
	 */
	private String sqxm;

	/**
	 * 申请进入区域
	 */
	private String sqjrqy;

	/**
	 * 申请进入区域1
	 */
	private String Sqjinruquyu;

	/**
	 * 附件
	 */
//	private String fj;

	/**
	 * A栋
	 */
	private String aa = "0";

	private String bb = "0";

	/**
	 * D栋
	 */
	private String ff = "0";


	/**
	 * D栋
	 */
	private String cc = "0";

	/**
	 * C栋
	 */
	private String rr = "0";

	/**
	 * E栋
	 */
	private String dd = "0";

	/**
	 * 宿舍/餐厅
	 */
	private String ee = "0";

	/**
	 * 外围联办
	 */
	private String gg = "0";

	/**
	 * 其他区域
	 */
	private String hh = "0";

	/**
	 * 1楼
	 */
	private String jj = "0";

	private String kk = "0";

	private String ll = "0";

	/**
	 * 外围
	 */
	private String qq = "0";

	private String ww = "0";

	private String tt = "0";

	/**
	 * 详细位置
	 */
	private String oo;

	/**
	 * 天台
	 */
	private String tiantai = "0";

	/**
	 * 联办
	 */
	private String lianban = "0";

	/**
	 * E2
	 */
	private String twoe = "0";

	/**
	 * E3
	 */
	private String threee = "0";

	/**
	 * E4
	 */
	private String foure = "0";

	/**
	 * E5
	 */
	private String fivee = "0";

	/**
	 * E6
	 */
	private String sixe = "0";

	/**
	 * E7
	 */
	private String seven = "0";

	/**
	 * E8
	 */
	private String eighte = "0";


}
