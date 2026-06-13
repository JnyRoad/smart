package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 访客审批白名单表
 *
 * @author wuling
 * @date 2020-12-29
 */
@Data
@TableName("SMT_VISITOR_APPROVAL_WHITE")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitorApprovalWhite extends Model<SmtVisitorApprovalWhite> {
	private static final long serialVersionUID = 3701290925100618075L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

    /**
   * 园区主键
   */
    private Integer parkId;

    /**
   * 员工工号
   */
    private String staffBadge;

    /**
   * 创建时间
   */
    private Date createTime;
}
