package com.tce.smart.platform.api.dto.resp.badge;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 厂牌挂失
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BadgeLossExcelRespDTO extends BaseDTO {

    /**
   * 主键
   */
	private Long id;
    /**
   * 员工工号
   */
	@Excel(name = "工号", isImportField = "true_st")
    private String badge;
    /**
   * 员工姓名
   */
	@Excel(name = "姓名", isImportField = "true_st")
    private String name;
	/**
	 * 园区名
	 */
	@Excel(name = "所属园区", isImportField = "true_st")
	private String parkName;
    /**
   * BU名
   */
	@Excel(name = "BU", isImportField = "true_st")
    private String compName;
    /**
   * 部门名
   */
	@Excel(name = "部门", isImportField = "true_st")
    private String depName;

    /**
   * 挂失时间
   */
	@Excel(name = "挂失时间", isImportField = "true_st")
    private LocalDateTime createTime;


}
