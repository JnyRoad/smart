package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 访客表
 *
 * @author liangyuan
 * @date 2019-04-11 15:57:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchTadayVisitorDetail extends Model<SearchTadayVisitorDetail> {
private static final long serialVersionUID = 1L;

/**
 * 访客的信息
 */
   private Long id;

   private String visitorName;

   private String visitorPhotoId;

   private String visitorPhoto;
   /**
  * 通过时间
  */
   private Date snapTime;

   private String parkName;
  /**
  *
  */
   private String snapPhotoId;

   private String snapPhoto;
	/**
	 * 通过区域
	 */
   private String areaName;

   private String visitorPhone;

   private String vehiclePlate;

   private String company;

    /**
     * 事由
     */
    private Integer cause;
    /**
     * 事由
     */
    private String causeDesc;

    /**
     * 身份证号
     */
    private String certNo;
    /**
     * 被访人的信息
     */

    private Long receptionistId;
    private String receptionistName;
    private String receptionistDept;
    private String receptionistPhone;

    /**
     *
     */
      private Date startTime;
      /**
     *
     */
      private Date endTime;
    /**
     * 当天的访客随行人员的信息和抓拍信息
     */
    private List<SnapTodayFellowVisitor> snapTodayFellowVisitorList;

}
