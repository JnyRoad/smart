package com.tce.smart.platform.core.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职工考勤异常申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:05
 */
@Data
@TableName("smt_attendance_exception")
@EqualsAndHashCode(callSuper = true)
public class SmtAttendanceException extends Model<SmtAttendanceException> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId
    private Integer id;
    /**
   *
   */
    private Long staffId;
    /**
   *
   */
    private String staffBadge;
    /**
   *
   */
    private String staffName;
    /**
   *
   */
    private LocalDateTime exceptionTime;
    /**
   * 异常原因
   */
    private String cause;
    /**
   * 流程编号
   */
    private String processid;
    /**
   * 创建时间
   */
    private LocalDateTime createtime;

}
