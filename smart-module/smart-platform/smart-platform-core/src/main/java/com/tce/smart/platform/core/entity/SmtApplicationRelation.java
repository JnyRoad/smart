package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:09
 */
@Data
@TableName("smt_application_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationRelation extends Model<SmtApplicationRelation> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId
    private Integer id;

	/**
	 * 应聘者号
	 */
    @NotBlank(message = "应聘者号不能为空")
	private String badge;
    /**
   * 姓名
   */
    @NotBlank(message = "姓名不能为空")
    private String name;
    /**
   * 与本人关系
   */
    @NotBlank(message = "关系不能为空")
    private String relation;
	/**
	 * 公司名称
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 课别
	 */
	private String className;
    /**
   * 应聘者ID
   */
    private Long applicationId;

    private Integer sex;

    /**
     * 详细关系
     */
    private String relationDetail;

    /**
     * 岗位名称
     */
    private String jobName;

}
