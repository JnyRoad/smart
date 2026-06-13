package com.tce.smart.businesstrip.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 出差日程数据
 *
 * @author mkwu
 * @date 2019-06-24
 */
@Data
@TableName("ccd_formtable_main_dt1")
public class CcdFormtableMainDt1 extends Model<CcdFormtableMainDt1> {

    private static final long serialVersionUID = -349692824607704979L;

	@TableField("MAINID")
	private Integer mainId;

    @TableField("DepartureTime")
    private String departureTime;

    @TableField("ArrivalTime")
    private String arrivalTime;

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
}
