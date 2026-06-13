package com.tce.smart.platform.core.entity.securityarea;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: 保密区预约记录
 * @date: 2020-07-30 8:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SECURITYAREA_ORDER")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAreaOrder extends Model<SmtSecurityAreaOrder> {

	private static final long serialVersionUID = -6976906744795319720L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 员工工号
	 */
	private String staffBadge;

	/**
	 * 到访区域
	 */
	private String visitArea;

	/**
	 * 来访事由
	 */
	private String visitType;

	/**
	 * 来访日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date comeTime;

	/**
	 * 离开日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date leaveTime;

	/**
	 * 供应商标识
	 */
	private Long supplierId;

	/**
	 * 受访者名称
	 */
	private String interviewName;

	/**
	 * 受访者电话
	 */
	private String interviewPhone;

	/**
	 * 陪同者名称
	 */
	private String escortName;

	/**
	 * 陪同者电话
	 */
	private String escortPhone;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 携带物品 多个物品已、号分隔
	 */
	private String carryGoods;

	/**
	 * 附件名称
	 */
	private String additionalName;

	/**
	 * OA系统流程Id
	 */
	private String processId;

	/**
	 * 状态 1.已申请 2.已通过 3.已退回
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
}
