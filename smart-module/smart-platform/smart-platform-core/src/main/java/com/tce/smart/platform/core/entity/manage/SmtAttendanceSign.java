package com.tce.smart.platform.core.entity.manage;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:43
 */
@Data
@TableName("SMT_ATTENDANCE_SIGN")
@EqualsAndHashCode(callSuper = true)
public class SmtAttendanceSign extends Model<SmtAttendanceSign> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 员工工号
   */
    private String badge;
    /**
   * 考核月份
   */
    private String checkDate;
    /**
   * 签收签名
   */
    private String signImg;
    /**
   * 创建时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 签收状态
   */
    private Integer signStatus;
    /**
   * 是否有异议
   */
    private Integer isObjection;
    /**
   * 异议
   */
    private String objection;
    /**
   * 签收时间
   */
    private LocalDateTime signDate;
	/**
	 * 通知状态
	 */
	private Integer noticeStatus;

}
