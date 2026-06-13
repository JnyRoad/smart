package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 当日的查询数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
public class SearchTodayVisitorDTO{
private static final long serialVersionUID = 1L;

   private String startTime;
   private String endTime;

}
