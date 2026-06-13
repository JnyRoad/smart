package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应聘者教育经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:36:50
 */
@Data
public class SmtApplicationEducationDTO implements Serializable {
private static final long serialVersionUID = -554219303984642385L;

    /**
   *
   */
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
