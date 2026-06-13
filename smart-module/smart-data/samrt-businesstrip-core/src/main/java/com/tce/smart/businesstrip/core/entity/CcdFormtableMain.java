package com.tce.smart.businesstrip.core.entity;

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
public class CcdFormtableMain extends Model<CcdFormtableMain> {


	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7525449936910067774L;

	@TableField("MAINID")
	private Integer mainId;
	@TableField("RequestId")
	private String requestId;
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
	private String tripBeginTime;
	@TableField("TripEndTime")
	private String tripEndTime;
	@TableField("ApplicationTime")
	private String applicationTime;
	@TableField("ConfirmName")
	private String confirmName;
	@TableField("TripReason")
	private String tripReason;
	@TableField("ActualReturnTime")
	private String actualReturnTime;
	@TableField("IsBooking")
	private Integer isBooking;
	@TableField("CustomerId")
	private String customerId;
	@TableField("CustomerName")
	private String customerName;
	@TableField("Remark")
	private String remark;

	//费用
	@TableField("BudgetBalance")
	private String budgetBalance;
	@TableField("HotelUnitPrice")
	private String hotelUnitPrice;
	@TableField("InHotelDays")
	private Integer inHotelDays;
	@TableField("TransCost")
	private String transCost;
	@TableField("ShortTranCost")
	private String shortTranCost;
	@TableField("MealCost")
	private String mealCost;


}
