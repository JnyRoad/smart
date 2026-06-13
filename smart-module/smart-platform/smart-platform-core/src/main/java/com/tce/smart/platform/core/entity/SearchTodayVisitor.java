package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客表
 *
 * @author liangyuan
 * @date 2019-04-11 15:57:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchTodayVisitor extends Model<SearchTodayVisitor> {
private static final long serialVersionUID = 1L;

/**
 * 访客的信息
 */
   private Long visitorId;

   private String visitorName;


   private String company;


   /**
   *
   */
    private Date startTime;
    /**
   *
   */
    private Date endTime;
    /**
     * 被访人的信息
     */
    private String receptionistDept;
    /**
     * 身份证号
     */
    private String certNo;

    private String parkName;

}
