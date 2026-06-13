package com.tce.smart.platform.core.entity.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@Data
@Builder
@AllArgsConstructor
@TableName("smt_settlement_info")
@EqualsAndHashCode(callSuper = true)
public class SmtSettlementInfo extends Model<SmtSettlementInfo> {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 园区id
	 */
	private Integer parkId;
	/**
	 * 工号
	 */
	private String badge;
	/**
	 * 姓名
	 */
	private String name;
	/**
	 * BU
	 */
	private String bu;
	/**
	 * 部门
	 */
	private String dept;
	/**
	 * 结算费用
	 */
	private BigDecimal fee;
	/**
	 * 离职时间
	 */
	private LocalDateTime leaveDate;
	/**
	 * 结算状态
	 */
	private Integer status;
	/**
	 * 结算时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
	/**
	 * 退宿时间
	 */
	private LocalDateTime quitDate;
	/**
	 * 上月退宿时间
	 */
	private LocalDateTime preCollect;
	/**
	 * 离职天数
	 */
	private Integer leaveDays;
	/**
	 * BU
	 */
	private String buName;
}
