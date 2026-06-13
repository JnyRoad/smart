package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外宿补贴撤销记录表
 * @author QIPEI
 *
 */
@Data
@TableName("smt_callowance_cancel_record")
@EqualsAndHashCode(callSuper = true)
public class SmtCallowanceCancelRecord extends Model<SmtCallowanceCancelRecord> {


	private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(value = "id",type = IdType.AUTO)
    private Integer id;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 补贴类型 10-外食补贴，11-外宿补贴
	 */
	private Integer xType;

	/**
	 * 补贴开始时间
	 */
	private Date startTime;

	/**
	 * 补贴结束时间
	 */
	private Date endTime;

	/**
	 * 补贴撤销时间
	 */
	private Date backDate;

	/**
	 * 是否撤销  0-否  1-是
	 */
	private Integer ifCancel;

	/**
	 * bu
	 */
	private String compId;
	/**
	 * 岗位id
	 */
	private String jobId;
	/**
	 * 部门id
	 */
	private String depId;

	/**
	 * 申请id
	 */
	private Integer appid;

	/**
	 * 薪资区域
	 */
	private Integer pzid;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 流程号
	 */
	private String processId;

	/**
	 * 补贴金额
	 */
	private String amount;

	/**
	 * 备注
	 */
	private String remark;

}
