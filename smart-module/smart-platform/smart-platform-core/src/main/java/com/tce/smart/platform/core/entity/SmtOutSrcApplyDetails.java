package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:41
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_OUT_SRC_APPLY_DETAILS")
@EqualsAndHashCode(callSuper = true)
public class SmtOutSrcApplyDetails extends Model<SmtOutSrcApplyDetails> {

	private static final long serialVersionUID = -3902331923944793619L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 申请单ID
	 */
	private Long applyId;
	/**
	 * 部门ID
	 */
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 员工工号
	 */
	private String badge;
	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * 手机号
	 */
	private String phone;
	/**
	 * 身份证号
	 */
	private String certno;
	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * 职层ID
	 */
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;
	/**
	 * 入职时间
	 */
	private Date entryDate;
	/**
	 * 派遣渠道
	 */
	private String dispatchChannel;
}
