package com.tce.smart.platform.core.vo;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
@Data
public class SmtSnapPersonDetailVO {

    /**
   * 主键
   */
    @TableId
    private Integer id;

    /**
   *
   */
    private String photoId;
    private String photo;

    /**
   *
   */
    private String personName;
    /**
     *
     */
    private String staffBadge;
    /**
   * compName BU名称
   */
    private String compName;
    /**
     * 部门名称
     */
    private String depName;
    /**
     * 岗位名称
     */
    private String jobName;
    /**
     * 职层名称
     */
    private String jcheName;
    /**
   * 内部员工状态
   */
    private Integer staffStatus;
    private String staffStatusDesc;
    /**
   *
   */
    private String areaName;
    /**
   * 出入类型1进门 2出门
   */
    private Integer eventType;
    private String eventTypeDesc;
    /**
   * 通过时间
   */
    private Date snapTime;
    /**
     * 所属单位     外部人员时展示
     */
    private String company;

    /**
   * personPhone
   */
    private String personPhone;

    /**
   *
   */
    private String snapPhotoId;
    private String snapPhoto;
    private Integer personType;

    private Integer parkId;
    /**
     * 出入园区名称
     */
    private String parkName;

	/**
	 * 体温
	 */
	private Double faceTemperature;

	/**
	 * 体温是否正常  1.正常 0.不正常
	 */
	private Integer isNormal;

	/**
	 * 设备ID
	 */
	private String deviceId;

	/**
	 * 设备名称
	 */
	private String deviceName;
}
