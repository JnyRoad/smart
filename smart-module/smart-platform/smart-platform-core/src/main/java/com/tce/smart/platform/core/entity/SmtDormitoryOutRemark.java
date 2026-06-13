package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 住宿备注表
 *
 * @author fushiping
 * @date 2020-12-29
 */
@Data
@TableName("SMT_DORMITORY_OUT_REMARK")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryOutRemark extends Model<SmtDormitoryOutRemark> {
	private static final long serialVersionUID = 4692701122505311026L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    /**
   * 员工住宿表ID
   */
    private Integer dorStaffId;

	/**
	 * 退宿记录表ID
	 */
    private Integer dorHistoryStaffId;

    /**
   * 离宿类型  1：出差   2：请假   3：调休
   */
    private Integer reasonType;

    /**
   * 开始日期
   */
    private Date startTime;

    /**
   * 结束日期
   */
    private Date endTime;

    /**
   *  备注
   */
    private String remark;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

}
