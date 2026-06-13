package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 水电上月止度修改历史记录表
 * @date: 2020-11-21 14:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SD_PREMHISTORY")
@EqualsAndHashCode(callSuper = true)
public class SmtSDPreMHistory extends Model<SmtSDPreMHistory> {
	private static final long serialVersionUID = 2741460830973596081L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 房间抄表记录或公摊表记录ID
	 */
	private Long mrId;


	/**
	 * 抄表详情记录ID
	 */
	private Long mrdetailId;


	/**
	 * 收费项目
	 */
	private Integer categoryId;

	/**
	 * 旧读数
	 */
	private Double oldNum;

	/**
	 * 新读数
	 */
	private Double newNum;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 抄表月份
	 */
	private Date meterMonth;

	/**
	 * 抄表类型 1.房间抄表 2.公摊抄表
	 */
	private Integer meterType;

	/**
	 * 本次抄表人
	 */
	private String userName;

	/**
	 * 上次抄表人
	 */
	private String preUserName;

	/**
	 * 添加时间
	 */
	private Date createTime;
}
