package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调查问卷 --问题选项表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_select")
@EqualsAndHashCode(callSuper = true)
public class SmtSelect  extends Model<SmtSelect>{


	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 问题id
	 */
	private Integer questionId;


	/**
	 * 选项内容
	 */
	private String answer;


}
