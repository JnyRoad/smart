package com.tce.smart.platform.core.entity;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职工补卡申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:19:37
 */
@Data
@TableName("smt_replace_application")
@EqualsAndHashCode(callSuper = true)
public class SmtReplaceApplication extends Model<SmtReplaceApplication> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId
    private Integer id;
    /**
   * 员工id
   */
    private Long staffId;
    /**
   * 员工号
   */
    private String staffBadge;
    /**
   * 员工姓名
   */
    private String staffName;
    /**
   * 考勤月份
   */
    private String workMonth;
    /**
     *补卡开始时间
     */
    private String startTime;
    /**
   * 流程编号
   */
    private String processId;
    /**
   * 补卡原因
   */
    private Integer cause;
    /**
   * 创建时间
   */
    private Date createTime;

    /**
     * 2段进
     */
    private String startDateTwo;
    /**
     * 2段出
     */
    private String endDateTwo;
    /**
     * 2段进是否跨天
     */
    private Integer startTwoCover;
    /**
     * 2段出是否跨天
     */
    private Integer endTwoCover;

    /**
     * 4段进
     */
    private String startDateFour;
    /**
     * 4段出
     */
    private String endDateFour;
    /**
     * 4段进是否跨天
     */
    private Integer startFourCover;
    /**
     * 4段出是否跨天
     */
    private Integer endFourCover;

    /**
     * 5段进
     */
    private String startDateFive;
    /**
     * 5段出
     */
    private String endDateFive;
    /**
     * 5段进是否跨天
     */
    private Integer startFiveCover;
    /**
     * 5段出是否跨天
     */
    private Integer endFiveCover;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件图片的id
     */
    private String photoId;
}
