package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷调查--问卷调查记录
 * @author 齐佩
 *
 */
@Data
@TableName("smt_paper_record")
@EqualsAndHashCode(callSuper = true)
public class SmtPaperRecord extends Model<SmtPaperRecord> {


	@TableId(value = "id", type = IdType.AUTO)

	private Integer id;

	/**
	 * 问卷id
	 */
	private Integer paperId;

	/**
	 * 问题id
	 */
	private Integer questionId;

	/**
	 * 答案
	 */
	private String answer;



	/**
	 * 员工号
	 */
	private String badge;


}
