package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 访客审批代理表
 *
 * @author wuling
 * @date 2020-12-29
 */
@Data
@TableName("SMT_VISITOR_APPROVAL_PROXY")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitorApprovalProxy extends Model<SmtVisitorApprovalProxy> {
	private static final long serialVersionUID = -2512692903675069739L;

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
   * 被访人员工工号
   */
    private String intervieweeBadge;

	/**
	 * 代理人员工工号
	 */
	private String proxyBadge;

    /**
   * 创建时间
   */
    private Date createTime;
}
