package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

/**
 * 员工教育经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:36:50
 */
@Data
@TableName("smt_staff_education")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffEducation extends Model<SmtStaffEducation> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 教育开始时间
   */
    @NotBlank(message = "教育开始时间不能为空")
    private String startTime;
    /**
   * 教育结束时间
   */
    @NotBlank(message = "教育结束时间不能为空")
    private String endTime;
    /**
   * 学校名称
   */
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;
    /**
   * 专业
   */
    @NotBlank(message = "专业不能为空")
    private String major;
    /**
   * 学历
   */
    @NotBlank(message = "学历不能为空")
    private String education;


    /**
     * 学位
     */
    private String degree;
    /**
   * 员工ID
   */
    private Long staffId;

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
