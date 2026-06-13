package com.tce.smart.platform.api.dto.resp.admittance;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 访客抓拍实体类
 *
 * @author
 * @date 2019-04-13 18:19:30
 */

@Data
public class SnapVisitorRespDTO implements Serializable {
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

}
