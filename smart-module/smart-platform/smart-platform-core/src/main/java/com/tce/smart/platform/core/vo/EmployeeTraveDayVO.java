package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *出差返回实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeTraveDayVO extends Model<EmployeeTraveDayVO> {
private static final long serialVersionUID = 1L;

private Integer mainId;

private String departureTime;

private String arrivalTime;

private String departureCity;

private String arrivalCity;

private Integer transportLargeClass;
private String transportLargeClassDesc;

private Integer transportSubClass;
private String transportSubClassDesc;

private Double averageTicketPrice;

private Double actualTicketPrize;

}
