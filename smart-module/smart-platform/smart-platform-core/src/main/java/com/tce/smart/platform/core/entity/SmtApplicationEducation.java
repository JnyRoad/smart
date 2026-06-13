package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应聘者教育经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:36:50
 */
@Data
@TableName("smt_application_education")
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationEducation extends Model<SmtApplicationEducation> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 教育开始时间
   */
    private String startTime;
    /**
   * 教育结束时间
   */
    private String endTime;
    /**
   * 学校名称
   */
    private String schoolName;
    /**
   * 专业
   */
    private String major;
    /**
   * 学历
   */
    private String education;

    /**
     * 学位
     */
    private String degree;

    /**
   * 应聘者ID
   */
    private Long applicationId;

    /**
     * 毕业类型
     */
    private Integer gradType;

    /**
     * 是否最高学历
     */
    private Integer isHighEduType;

    /**
     * 是否最高学位
     */
    private Integer isHighDegreeType;

}
