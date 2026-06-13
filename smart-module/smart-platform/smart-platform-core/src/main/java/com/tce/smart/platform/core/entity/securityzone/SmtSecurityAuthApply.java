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
	 * 是否发送短信
	 */
    private Integer isMsg;

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
}
