package com.tce.smart.platform.api.dto.resp.manage;

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
public class RechargePageRespDTO extends BaseDTO {
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
   * 餐补结算
   */
    private BigDecimal account;
    /**
   * 结算时间
   */
    private LocalDateTime createTime;
    /**
   * 同步状态
   */
    private Integer syncStatus;
    /**
   * 员工状态
   */
    private Integer staffStatus;

	private String staffStatusDesc;
    /**
   * 考勤月份
   */
    private LocalDate checkMonth;
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
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 福利层次
	 */
	private String welfareLevel;

}
