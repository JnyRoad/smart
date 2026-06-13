package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 公摊水电记录
 * @date: 2020-09-29 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_COMMON_SD")
@EqualsAndHashCode(callSuper = true)
public class SmtCommonSD extends Model<SmtCommonSD> {

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
     * 水电表名称
	 */
	private String sdName;

	/**
     * 收费项目ID 1.热水 2.冷水 3.电
	 */
	private Integer categoryId;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;

	/**
     * 房间列表 以“,”(逗号)分隔
	 */
	private String roomList;

	/**
	 * 状态 1.可用 0.不可用
	 */
	private Integer status;

	/**
     * 创建时间
	 */
	private Date createTime;

    /**
     * 最后更新时间
	 */
	private Date updateTime;
}
