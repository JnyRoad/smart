package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:17
 */
@Data
@Builder
@TableName("smt_security_task_details")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityTaskDetails extends Model<SmtSecurityTaskDetails> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 工号
   */
    private String staffBadge;
	/**
	 * 员工id
	 */
    private Long staffId;
    /**
   * 姓名
   */
    private String staffName;
    /**
   * 申请区域名
   */
    private String areaName;
    /**
   * 下发状态
   */
    private Integer status;
    /**
   * 备注
   */
    private String remark;
    /**
   * 图片code
   */
    private String imgCode;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * 修改时间
   */
    private LocalDateTime updateTime;

	/**
	 * 申请ID
	 */
    private Long applyId;

	/**
	 * 权限策略ID
	 */
	private Integer authId;

	/**
	 * 权限策略名
	 */
	private String authName;

	/**
	 * 保密区权限下发批次号，WAIT 明细以此字段作为持久化待消费命令标记
	 */
	private Long dispatchBatchId;

}
