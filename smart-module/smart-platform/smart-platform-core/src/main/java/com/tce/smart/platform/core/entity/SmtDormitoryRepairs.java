package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @description: SmtDormitoryRepairs
 * @date: 2020-07-20 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_DORMITORY_REPAIRS")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryRepairs extends Model<SmtDormitoryRepairs> {

	private static final long serialVersionUID = -4193439100249527516L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 员工工号
	 */
	private String staffBadge;
	/**
	 * 范围类型
	 */
	private Integer rangeType;
	/**
	 * 报修类型
	 */
	private Integer repairType;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;

	/**
	 * 房间名称
	 */
	private String roomName;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 故障描述
	 */
	private String faultDesc;

	/**
	 * 故障图片地址
	 */
	private String faultImgs;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 维修结果
	 */
	private String result;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 最后更新时间
	 */
	private Date updateTime;
}
