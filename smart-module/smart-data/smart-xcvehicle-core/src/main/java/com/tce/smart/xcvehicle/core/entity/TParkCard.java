package com.tce.smart.xcvehicle.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆信息表
 *
 * @author wuling
 * @date 2021-01-19
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("t_parkcard")
public class TParkCard extends Model<TParkCard> {
	private static final long serialVersionUID = -7852766247744894274L;

	@TableField("CID")
    private Integer cId;

    @TableField("UserName")
    private String userName;

	@TableField("CardNo")
	private String cardNo;

	@TableField("CTID")
	private Integer ctId;

	@TableField("FCTCode")
	private Integer fctCode;

	@TableField("CardState")
	private Integer cardState;

	@TableField("RegisterDate")
	private LocalDateTime registerDate;

	@TableField("IOState")
	private Integer iOState;

	@TableField("PCardNo")
	private String pCardNo;

	@TableField("sendState")
	private Integer sendState;

	@TableField("Address")
	private String address;

	@TableField("Phone")
	private String phone;

	@TableField("CarNo")
	private String carNo;

	@TableField("Carlocate")
	private String carlocate;

	@TableField("DRiveNo")
	private String dRiveNo;

	@TableField("CardMoney")
	private BigDecimal cardMoney;

	@TableField("CarColor")
	private String carColor;

	@TableField("StartDate")
	private LocalDateTime startDate;

	@TableField("ValidDate")
	private LocalDateTime validDate;

	@TableField("Remark")
	private String remark;

	@TableField("CUser")
	private String cUser;

	@TableField("CDate")
	private String cDate;

	@TableField("GrantDesc")
	private String grantDesc;

	@TableField("ChargeType")
	private Integer chargeType;

	@TableField("bluetoothstate")
	private Integer bluetoothstate;

	@TableField("GrantState")
	private Integer grantState;

	@TableField("CarModel")
	private String carModel;

	@TableField("FeePeriod")
	private String feePeriod;

	@TableField("AreaID")
	private Integer areaID;

	@TableField("TimeGroupNumber")
	private Integer timeGroupNumber;

	@TableField("LimitDayType")
	private Integer limitDayType;

	@TableField("HCardNo")
	private String hCardNo;
}
