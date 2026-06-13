package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客抓拍实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */

@EqualsAndHashCode(callSuper = true)
public class SnapVisitor extends Model<SnapVisitor> {
private static final long serialVersionUID = 1L;

	/**
	 * 抓拍的图片id
	 */
	private String snapPhotoId;

	private String snapPhoto;

	/**
	 * 抓拍的时间
	 */
	private Date snapTime;

	public String getSnapPhotoId() {
		return snapPhotoId;
	}

	public void setSnapPhotoId(String snapPhotoId) {
		this.snapPhotoId = snapPhotoId;
	}

	public String getSnapPhoto() {
		return snapPhoto;
	}

	public void setSnapPhoto(String snapPhoto) {
		this.snapPhoto = snapPhoto;
	}

	public Date getSnapTime() {
		return snapTime;
	}

	public void setSnapTime(Date snapTime) {
		this.snapTime = snapTime;
	}


}
