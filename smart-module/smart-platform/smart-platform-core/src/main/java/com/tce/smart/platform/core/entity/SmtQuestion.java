package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷调查--问题表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_question")
@EqualsAndHashCode(callSuper = true)
public class SmtQuestion extends Model<SmtQuestion> {


	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 问卷id
	 */
	private Integer paperId;

	/**
	 * 问题标题
	 */
	private String title;



	/**
	 * 问题类型 0-单选 1-多选 2-问答题
	 */
	private Integer type;






}
