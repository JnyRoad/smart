package com.tce.smart.platform.core.dto;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 调休审批申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddBreakOffApplicationApprovalDTO extends Model<AddBreakOffApplicationApprovalDTO> {
	private static final long serialVersionUID = 1L;


	/**
	 *
	 */
	private String staffBadge;

	/**
	 *
	 */
	private Integer restType;
	/**
	 *
	 */
	private Date restDate;
	/**
	 *
	 */
	private Date workDate;
	/**
	 * 调休时长
	 */
	private Integer restCount;
	/**
	 *调休备注
	 */
	private String vacateDesc;

}
