package com.tce.smart.platform.core.vo;

import java.sql.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出差日程数据
 *
 * @author mkwu
 * @date 2019-06-24
 */
@Data
@TableName("ccd_formtable_main_dt1")
@EqualsAndHashCode(callSuper = true)
public class CcdFormtableMainDt1VO extends Model<CcdFormtableMainDt1VO> {

    private static final long serialVersionUID = 1L;

	@TableField("MAINID")
	private Integer mainId;

    @TableField("DepartureTime")
    private Date departureTime;

    @TableField("ArrivalTime")
    private Date arrivalTime;

    @TableField("DepartureCity")
    private String departureCity;

    @TableField("ArrivalCity")
    private String arrivalCity;

    @TableField("TransportLargeClass")
    private Integer transportLargeClass;

    @TableField("TransportSubClass")
    private Integer transportSubClass;

    @TableField("AverageTicketPrice")
    private Double averageTicketPrice;

    @TableField("ActualTicketPrize")
    private Double actualTicketPrize;


    @TableField("BudgetBalance")
    private Double budgetBalance;


    @TableField("HotelUnitPrice")
    private Double hotelUnitPrice;

    @TableField("InHotelDays")
    private Integer inHotelDays;

    @TableField("TransCost")
    private Double transCost;


    @TableField("ShortTranCost")
    private Double shortTranCost;

    @TableField("MealCost")
    private Double mealCost;
}
