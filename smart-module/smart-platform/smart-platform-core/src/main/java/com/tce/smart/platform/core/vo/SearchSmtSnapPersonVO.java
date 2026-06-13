package com.tce.smart.platform.core.vo;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

/**
 * 查询人员抓拍返回实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
@SuppressWarnings("serial")
@Data
public class SearchSmtSnapPersonVO extends Model<SearchSmtSnapPersonVO>{

    /**
   * 主键
   */
    private Integer id;

    /**
     *
     */
    private Long personId;
    /**
   *
   */
    private String personName;
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
   *
   */
    private String areaName;
    /**
   * 出入类型1进门 2出门
   */
    private Integer eventType;
    /**
   * 通过时间
   */
    private Date snapTime;
    /**
     * 所属单位     外部人员时展示
     */
    private String company;

    /**
   *
   */
    private String snapPhotoId;
    private String snapPhoto;

    /**
     * 所属园区
     */
    private String parkName;


	/**
	 * 体温
	 */
	private Double faceTemperature;

	/**
	 * 体温是否正常 1.正常 0.不正常
	 */
	private Integer isNormal;

	/**
	 * 设备Id
	 */
	private String deviceId;

	/**
	 * 设备名称
	 */
	private String deviceName;

	private String badge;
}
