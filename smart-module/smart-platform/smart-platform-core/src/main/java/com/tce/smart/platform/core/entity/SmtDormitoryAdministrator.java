package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: SmtDormitoryAdministrator
 * @Descripition: 宿舍管理员
 * @Auther: guohongtai
 * @Date: 2020-10-14 15:21
 */
@Data
@TableName("smt_dormitory_administrator")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryAdministrator extends Model<SmtDormitoryAdministrator> {
	private static final long serialVersionUID = 1L;
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	private Integer parkId;
	private String badgeOne;
	private String badgeTwo;
	private String badgeThree;
	private String badgeFour;

}
