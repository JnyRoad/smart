package com.tce.smart.platform.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调查问卷范围
 * @author 齐佩
 *
 */
@Data
@TableName("smt_paper_bu")
@EqualsAndHashCode(callSuper = true)
public class SmtPaperBu  extends Model<SmtPaperBu>{

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	private Integer paperId;

	private Integer compId;

}
