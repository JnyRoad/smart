package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工工作经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:00
 */
@Data
@TableName("smt_staff_work")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffWork extends Model<SmtStaffWork> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   *
   */
    private String startTime;
    /**
   *
   */
    private String endTime;
    /**
   * 公司名称
   */
    private String company;
    /**
   * 职位
   */
    private String jobName;

    /**
   * 负责人名称
   */
    private String personLiable;
    /**
   * 负责人电话
   */
    private String phone;
    /**
   * 员工ID
   */
    private Long staffId;

}
