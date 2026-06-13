package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:37
 */
@Data
@TableName("smt_security_apply_person")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityApplyPerson extends Model<SmtSecurityApplyPerson> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 员工工号
   */
    private String badge;
    /**
   * 员工id
   */
    private Integer staffId;
    /**
   * 员工姓名
   */
    private String staffName;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 申请区域id
   */
    private String areaId;
    /**
   * 申请区域名
   */
    private String areaName;
    /**
   * 申请区域明细
   */
    private String authDetails;
    /**
   * 申请表ID
   */
    private Long applyId;

}
