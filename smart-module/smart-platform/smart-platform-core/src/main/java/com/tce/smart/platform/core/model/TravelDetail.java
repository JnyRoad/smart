package com.tce.smart.platform.core.model;

import java.sql.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class TravelDetail extends BaseVO{

    private static final long serialVersionUID = 1L;

    //员工号
    private String employeeId;
    //员工姓名
    private String employeeName;
    //员工公司
    private String compName;
    //员工部门
    private String deptName;
    //员工岗位
    private String jobName;
	//代办人
	private String agentName;
	//代办人工号
	private String agentBadge;
	//员工工号
	private String pedestrianBadge;
	//申请部门编码
	private String depId;
	//出行时间
	private String pedestrianTime;
	//出差类型
	private Integer tripType;
	//出差类型描述
	private String tripTypeDesc;
	//出差开始时间
	private Date tripBeginTime;
	//出差结束时间
	private Date tripEndTime;
	//申请时间
	private Date applicationTime;
	//出差达成效果确认人员
	private String confirmName;
	//出差原因
	private String tripReason;
	//实际返回时间
	private Date actualReturnTime;
	//是否定机票
	private Integer isBooking;
	//是否定机票描述
	private String isBookingDesc;
	//CRM客户
	private String customerId;
	//客户名称
	private String customerName;
	//备注
	private String remark;

	//费用
	//预算项余额
	private String budgetBalance;
	//酒店单价
	private String hotelUnitPrice;
	//酒店住宿天数
	private Integer inHotelDays;
	//交通费用
	private String transCost;
	//短途交通费用
	private String shortTranCost;
	//餐补费用
	private String mealCost;
}
