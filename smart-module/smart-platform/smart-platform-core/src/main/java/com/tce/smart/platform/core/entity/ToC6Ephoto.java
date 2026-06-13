package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("to_c6_ephoto")
@EqualsAndHashCode(callSuper = true)
public class ToC6Ephoto extends Model<ToC6Ephoto> {
    private static final long serialVersionUID = 1L;

    /**
	 * ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 员工号
	 */
   private String empNo;

   /**
    * 员工姓名
    */
   private String Name;

   /**
    * 图片byte
    */
   private byte[] photo;

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
}
