package com.tce.smart.platform.core.entity;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
@Data
@TableName("smt_snap_person")
@EqualsAndHashCode(callSuper = true)
public class SmtSnapPerson extends Model<SmtSnapPerson> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 设备ID
   */
    private String deviceId;
    /**
   * 设备通道号
   */
    private Integer channelNo;
    /**
   *
   */
    private Integer areaId;
    /**
   *
   */
    private String areaName;
    /**
   *
   */
    private Integer eventType;
    /**
   * 通过时间
   */
    private Date snapTime;
    /**
   *
   */
    private String snapPhotoId;
    /**
   *
   */
    private String photoId;
    /**
   *
   */
    private Long personId;
    /**
   *
   */
    private String personName;
    /**
   * 1:员工；2：访客；
   */
    private Integer personType;
    /**
   * 抓拍人员的的员工了类型
   */
    private String personPhone;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 体温
	 */
	private Double faceTemperature;
}
