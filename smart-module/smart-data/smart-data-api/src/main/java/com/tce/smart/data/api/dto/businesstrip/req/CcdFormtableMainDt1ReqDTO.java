package com.tce.smart.data.api.dto.businesstrip.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 出差日程数据
 *
 * @author mkwu
 * @date 2019-06-24
 */
@Data
public class CcdFormtableMainDt1ReqDTO implements Serializable {

    private static final long serialVersionUID = -390223334873000174L;

	private Integer mainId;

    private String departureTime;

    private String arrivalTime;

    private String departureCity;

    private String arrivalCity;

    private Integer transportLargeClass;

    private Integer transportSubClass;

    private Double averageTicketPrice;

    private Double actualTicketPrize;
}
