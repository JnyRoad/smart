package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 退宿申请表
 *
 * @author FUSHIPING
 * @date
 */
@Data
@TableName("SMT_DORMITORY_QUIT_APPLY")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryQuitApply extends Model<SmtDormitoryQuitApply> {

	private static final long serialVersionUID = 1L;

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
	 * 申请人工号
	 */
	private String badge;
	/**
	 * 申请人姓名
	 */
	private String name;
	/**
	 * 申请人ID
	 */
	private Long staffId;
	/**
	 * 退宿房间ID
	 */
	private String roomIds;
	/**
	 *  退宿原因
	 */
	private Integer quitReason;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 上传图片
	 */
	private String imgs;
	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 安保人员姓名
	 */
	private String securityStaff;

	/**
	 * 保安备注
	 */
	private String securityRemark;
	/**
	 * 申请离开时间
	 */
	private LocalDateTime applyLeaveTime;
	/**
	 * 离开时间
	 */
	private LocalDateTime leaveTime;

	private String smsCode;

	/**
	 * 审批通过时间
	 */
	private LocalDateTime passTime;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 是否处理 0 未处理 1 已处理
	 */
	private Integer isHandle;

}
