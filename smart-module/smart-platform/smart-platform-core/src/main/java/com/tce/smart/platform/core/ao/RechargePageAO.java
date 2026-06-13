package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@Data
public class RechargePageAO extends BaseDTO {
private static final long serialVersionUID = 1L;

    /**
   * 考勤月份
   */
    private String checkMonth;
    /**
   * 所属园区
   */
    private Integer parkId;

	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * buname
	 */
	private List<String> compIds;


	/**
	 * buname
	 */
	private String compId;
	/**
	 * 是否生成 2 未生成 1 已生成
	 */
	private Integer isAccount;
	/**
	 * 部门名称
	 */
	private String depId;

	private String badges;
	/**
	 * 充值名单类型
	 */
	private Integer rechargeType;

	private Integer syncStatus;

	private List<Integer> parkIds;

	/**
	 * 入职开始日期
	 */
	private String startEntryTime;

	/**
	 * 入职结束日期
	 */
	private String endEntryTime;

	/**
	 * 工号集合
	 */
	private List<String> badgeList;

}
