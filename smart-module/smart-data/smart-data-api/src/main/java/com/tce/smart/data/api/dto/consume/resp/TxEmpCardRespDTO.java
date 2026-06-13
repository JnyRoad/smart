package com.tce.smart.data.api.dto.consume.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人事信息表
 *
 * @author fushiping
 * @date 2020-7-09
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class TxEmpCardRespDTO extends BaseDTO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 卡流水号
	 */
	private Long CardID;

	/**
	 * 员工姓名
	 */
	private String EmpName;

	/**
	 * 厂牌状态
	 */
	private Integer CardStatusID;

	/**
	 * 厂牌id
	 */
	private String EmpSysID;

	private String cardDispNo;
}
