package com.tce.smart.platform.core.entity.securityzone;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("smt_security_auth_apply")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityAuthApply extends Model<SmtSecurityAuthApply> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
    /**
   * 流水号
   */
    private String serialNum;
    /**
   * oa单号
   */
    private String processId;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;
    /**
   * oa状态
   */
    private Integer oaStatus;
    /**
   * 下发状态
   */
    private Integer deviceStatus;
    /**
   * 下发总数
   */
    private Integer totalNum;
    /**
   * 申请区域id
   */
    private String areaId;
    /**
   * 申请区域名
   */
    private String areaName;
    /**
   * 备注
   */
    private String remark;
    /**
   * 园区id
   */
    private Integer parkId;

	/**
	 * 申请人工号
	 */
    private String applyBadge;

	/**
	 * 微信推送状态：0-未发送，1-已发送，2-连续失败达上限已放弃（不再入扫）
	 */
    private Integer isMsg;

	/**
	 * 微信推送失败次数；达到上限（SmtSecurityAuthApplyServiceImpl.MAX_MSG_RETRY）后
	 * is_msg 置 2（失败放弃）不再重试。列由 manual 脚本
	 * 2026-07-06-security-msg-retry.sql 添加，DEFAULT 0。
	 */
	private Integer msgRetryCount;

	/**
	 * 授权进入区域选项
	 */
	private String areaType;

	/**
	 * 授权进入区域详情
	 */
	private String permitArea;

	/**
	 * 授权进入旧厂区域详情
	 */
	private String permitOldArea;

	/**
	 * 当前保密区权限下发批次号，用于标识本申请最新受理的下发命令
	 */
	private Long currentDispatchBatchId;
}
