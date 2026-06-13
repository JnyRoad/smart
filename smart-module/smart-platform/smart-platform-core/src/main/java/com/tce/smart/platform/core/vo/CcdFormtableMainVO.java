package com.tce.smart.platform.core.vo;

import java.sql.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出差数据
 *
 * @author liangyuan
 * @date 2019-06-24
 */
@Data
@TableName("ccd_formtable_main")
@EqualsAndHashCode(callSuper = true)
public class CcdFormtableMainVO extends Model<CcdFormtableMainVO> {


	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7525449936910067774L;

	@TableField("MAINID")
	private Integer mainId;
	@TableField("ProcessNumber")
	private String processNumber;
	@TableField("AgentName")
	private String agentName;
	@TableField("AgentBadge")
	private String agentBadge;
	@TableField("Depid")
	private String depId;
	@TableField("Depname")
	private String depName;
	@TableField("compname")
	private String compName;
	@TableField("PedestrianName")
	private String pedestrianName;
	@TableField("PedestrianBadge")
	private String pedestrianBadge;
	@TableField("PedestrianTime")
	private String pedestrianTime;
	@TableField("TripType")
	private Integer tripType;
	@TableField("Certno")
	private String certNo;
	@TableField("Email")
	private String email;
	@TableField("Mobile")
	private String mobile;
	@TableField("TripBeginTime")
	private Date tripBeginTime;
	@TableField("TripEndTime")
	private Date tripEndTime;
	@TableField("ApplicationTime")
	private Date applicationTime;
	@TableField("ConfirmName")
	private String confirmName;
	@TableField("TripReason")
	private String tripReason;
	@TableField("ActualReturnTime")
	private Date actualReturnTime;
	@TableField("IsBooking")
	private Integer isBooking;
	@TableField("CustomerId")
	private String customerId;
	@TableField("CustomerName")
	private String customerName;
	@TableField("Remark")
	private String remark;
}
