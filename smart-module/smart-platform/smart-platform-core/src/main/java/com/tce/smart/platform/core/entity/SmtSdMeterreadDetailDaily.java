package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @description: 水电抄表日结算
 * @date: 2020-07-10 8:51
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SD_METERREADDETAIL_DAILY")
@EqualsAndHashCode(callSuper = true)
public class SmtSdMeterreadDetailDaily extends Model<SmtSdMeterreadDetailDaily> {
	private static final long serialVersionUID = -3902331923944793619L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 水电表ID
	 */
	private Long meterId;
	/**
	 * 昨日使用
	 */
	private Double preNum;
	/**
	 * 今日使用
	 */
	private Double curNum;
	/**
	 * 收费项目
	 */
	private Integer categoryId;
	/**
	 * 房间id
	 */
	private Integer roomId;
	/**
	 * 房间名称
	 */
	private String roomName;
	/**
	 * 生成备注
	 */
	private String remark;
	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
	/**
	 * 修改时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;
}
