package com.tce.smart.data.api.dto.businesstrip.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

import java.sql.Date;

/**
 * 出差数据
 *
 * @author liangyuan
 * @date 2019-06-24
 */
@Data
public class CcdFormtableMainReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7525449936910067774L;

	private Integer mainId;
	private String requestId;
	private String processNumber;
	private String agentName;
	private String agentBadge;
	private String depId;
	private String depName;
	private String compName;
	private String pedestrianName;
	private String pedestrianBadge;
	private String pedestrianTime;
	private Integer tripType;
	private String certNo;
	private String email;
	private String mobile;
	private Date tripBeginTime;
	private Date tripEndTime;
	private Date applicationTime;
	private String confirmName;
	private String tripReason;
	private Date actualReturnTime;
	private Integer isBooking;
	private String customerId;
	private String customerName;
	private String remark;

	//费用
	private String budgetBalance;
	private String hotelUnitPrice;
	private Integer inHotelDays;
	private String transCost;
	private String shortTranCost;
	private String mealCost;


}
