package com.tce.smart.platform.api.dto.req.manage;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@Data
public class RechargePageReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;
    /**
   * 所属园区
   */
    private Integer parkId;

    private List<Long> ids;

	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * buname
	 */
	private String compId;

	/**
	 * buname
	 */
	private List<String> compIds;
	/**
	 * 部门名称
	 */
	private String depId;
	/**
	 * 充值名单类型
	 */
	private Integer rechargeType;
	/**
	 * 同步状态
	 */
	private Integer syncStatus;

	/**
	 * 考核月份
	 */
	private String checkMonth;

	private String badge;

	/**
	 * 入职开始日期
	 */
	private String startEntryTime;

	/**
	 * 入职结束日期
	 */
	private String endEntryTime;

	/**
	 * 是否生成 2 未生成 1 已生成
	 */
	private Integer isAccount;

}
