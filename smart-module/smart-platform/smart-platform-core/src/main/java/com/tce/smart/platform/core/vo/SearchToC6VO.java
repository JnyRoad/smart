package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;

@Data
public class SearchToC6VO {

private Integer id;

	/**
	 * 员工号
	 */
   private String empNo;

   /**
    * 员工姓名
    */
   private String Name;
   private String photos;


   /**
    * 是否已同步 0-未同步  1-已同步
    */
   private Integer isDispose;

   /**
    * 创建时间
    */
   private Date createTime;

   /**
    * 更新时间
    */
   private Date updateTime;

   private byte[] photo;

}
