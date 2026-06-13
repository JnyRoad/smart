package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_OUT_SRC_APPLY")
@EqualsAndHashCode(callSuper = true)
public class SmtOutSrcApply extends Model<SmtOutSrcApply> {

	private static final long serialVersionUID = -3902331923944793619L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 申请时间
	 */
	private Date createTime;

	/**
	 * 状态：0、待审批；1、已通过、2、已拒绝
	 */
	private Integer status;

	/**
	 * 申请人数
	 */
	private Integer applyNum;

	/**
	 * BU Id
	 */
	private String compId;
	/**
	 * BU name
	 */
	private String compName;

	/**
	 * 拒绝原因
	 */
	private String reason;

	/**
	 * 申请人用户ID
	 */
	private Integer applyUserId;

	/**
	 * 审批人用户ID
	 */
	private Integer approverId;

	/**
	 * 审批人姓名
	 */
	private String approver;

	/**
	 * 审批时间
	 */
	private Date approverTime;

	/**
	 * 申请人姓名
	 */
	private String applyUserName;
}
