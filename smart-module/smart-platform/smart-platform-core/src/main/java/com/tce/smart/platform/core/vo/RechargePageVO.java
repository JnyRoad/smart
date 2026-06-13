package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@Data
public class RechargePageVO extends BaseDTO {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
   * 员工工号
   */
    private String badge;

	/**
	 * 备注
	 */
    private String blank;
    /**
   * 餐补结算
   */
    private BigDecimal account;
    /**
   * 入职时间
   */
    private LocalDate createTime;
    /**
   * 同步状态
   */
    private Integer syncStatus;
    /**
   * 员工状态
   */
    private Integer staffStatus;
    /**
   * 考勤月份
   */
    private String checkMonth;
    /**
   * 应出勤
   */
    private Double shouldOn;
    /**
   * 实出勤
   */
    private Double actualOn;
    /**
   * 餐补标准
   */
    private BigDecimal standard;
    /**
   * 所属园区名
   */
    private String parkNames;

	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * buname
	 */
	private String compName;

	private String compId;
	/**
	 * 部门名称
	 */
	private String depName;

	private String depId;
	/**
	 * 福利层次
	 */
	private String welfareLevel;

}
