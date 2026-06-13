package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 17:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SD_METERREADDETAIL_CHANGE")
@EqualsAndHashCode(callSuper = true)
public class SmtSdMeterreadDetailChange extends Model<SmtSdMeterreadDetailChange> {
	private static final long serialVersionUID = -3902331923944793619L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 上月止度
	 */
	private Double preMonthNum;

	/**
	 * 本月止度
	 */
	private Double curMonthNum;

	/**
	 * 收费项目
	 */
	private Integer categoryId;

	/**
	 * 房间号标识
	 */
	@NotBlank(message="房间号标识不能为空")
	private Integer roomId;

	/**
	 * 抄表月份
	 */
	@NotBlank(message="抄表月份不能为空")
	@JsonFormat(pattern="yyyy-MM")
	private Date meterMonth;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
}
