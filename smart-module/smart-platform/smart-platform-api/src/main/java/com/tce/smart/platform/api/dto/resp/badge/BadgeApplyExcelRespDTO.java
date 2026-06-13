package com.tce.smart.platform.api.dto.resp.badge;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 厂牌补领
 *
 * @author fushiping
 * @date 2020-07-07 11:47:58
 */
@Data
public class BadgeApplyExcelRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;


    /**
   * 员工工号
   */
	@Excel(name = "工号")
    private String badge;
    /**
   * 员工姓名
   */
	@Excel(name = "员工姓名")
    private String name;
    /**
   * BU名
   */
	@Excel(name = "BU")
    private String compName;
    /**
   * 部门名
   */
	@Excel(name = "部门")
    private String depName;
	/**
	 * 所属园区
	 */
	@Excel(name = "所属园区")
	private String parkName;
    /**
   * 申请原因
   */
	@Excel(name = "申请原因")
    private String reason;

    /**
   * 起薪日期
   */
	@Excel(name = "申请时间")
    private LocalDateTime createTime;
	/**
	 * 办理状态
	 */
	@Excel(name = "办理状态")
	private String state;
	/**
	 * 个人扣款
	 */
	@Excel(name = "厂牌价格")
    private BigDecimal price;


}
