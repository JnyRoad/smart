package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

/**
 * @description: 派遣单位与C6部门的对应关系
 * @date: 2021-01-19
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_EXDEPT_C6")
@EqualsAndHashCode(callSuper = true)
public class SmtExDeptC6 extends Model<SmtExDeptC6> {


	/**
	 * 派遣单位记录Id
	 */
	private Long dId;

	/**
	 * C6部门编号
	 */
	private String c6DptNo;

}
