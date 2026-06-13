package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:26
 */
@Data
@TableName("smt_msg_temp_person")
@EqualsAndHashCode(callSuper = true)
public class SmtMsgTempPerson extends Model<SmtMsgTempPerson> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7273380905173585457L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;
	/**
	 * 模板ID
	 */
	private Integer tempId;
	/**
	 * 工号
	 */
	private String badge;
	/**
	 * 姓名
	 */
	private String name;
}
