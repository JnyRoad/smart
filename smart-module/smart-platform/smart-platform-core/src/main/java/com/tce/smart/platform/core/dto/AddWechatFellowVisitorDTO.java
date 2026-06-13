package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.List;

/**
 * 添加随行人员的添加数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
public class AddWechatFellowVisitorDTO {
private static final long serialVersionUID = 1L;


	/**
	 * 访客的id
	 */
   private Long visitId;
   /**
    * 随行人员的id
    */
   private List<SaveFellowWechatVisitorDTO> followList;
}
