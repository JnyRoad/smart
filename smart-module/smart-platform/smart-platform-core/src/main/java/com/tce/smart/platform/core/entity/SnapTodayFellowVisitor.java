package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 随行人员抓拍实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SnapTodayFellowVisitor extends Model<SnapTodayFellowVisitor> {
private static final long serialVersionUID = 1L;


/*	 private Long id;
    *//**
   *
   *//*
    private String fellowName;
    *//**
   *
   *//*
    private String fellowPhotoId;

	*//**
	 * 抓拍的图片id
	 *//*
	private String snapPhotoId;

	*//**
	 * 抓拍的时间
	 *//*
	private Date snapTime;*/

	/**
	 * 抓拍图片的base64
	 */
	private String snapPhoto;
	 /**
     * 图片的base64位
     */
    private String fellowPhoto;
}
