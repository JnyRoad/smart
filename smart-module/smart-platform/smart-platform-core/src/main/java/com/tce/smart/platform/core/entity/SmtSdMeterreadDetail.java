package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 水电抄表明细表
 * @date: 2020-07-10 8:51
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SD_METERREADDETAIL")
@EqualsAndHashCode(callSuper = true)
public class SmtSdMeterreadDetail extends Model<SmtSdMeterreadDetail> {
	private static final long serialVersionUID = -3902331923944793619L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 房间抄表记录标识
	 */
	@NotBlank(message="房间抄表记录标识不能为空")
	private Long mrId;

	/**
	 * 上月止度
	 */
	private Double preMonthNum;

	/**
	 * 上月止度修正数据
	 */
	private Double revPreMonthNum;

	/**
	 * 本月止度
	 */
	private Double curMonthNum;

	/**
	 * 收费项目
	 */
	private Integer categoryId;

	/**
	 * 生成明细时的标准用量
	 */
	private Double curStdQty;

	/**
	 * 生成明细时的超出单价
	 */
	private BigDecimal curOverFee;

	/**
	 * 本月入住总天数
	 */
	private Integer totalStayDays;

	/**
	 * 本次抄表开始时间
	 */
	private Date startTime;

	/**
	 * 本次抄表结束时间
	 */
	private Date endTime;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 上月止度是否修正过 0.未修正 1.已修正
	 */
	private Integer isRevise;

	/**
	 * 抄表人员
	 */
	private String meterUser;
}
