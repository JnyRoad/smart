package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * App的查询数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
public class SearchAppVisitorDTO{
private static final long serialVersionUID = 1L;

   private String staffBadge; //员工的员工号
   private Integer visitListType;// 预约的类型 1：我发起的预约，2待我审批

}
