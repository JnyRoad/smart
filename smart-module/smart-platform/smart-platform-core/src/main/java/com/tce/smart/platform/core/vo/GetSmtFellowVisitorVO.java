package com.tce.smart.platform.core.vo;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetSmtFellowVisitorVO extends Model<GetSmtFellowVisitorVO> {
	private static final long serialVersionUID = 1L;


	private Long id;
	/**
	 *
	 */
	private String fellowName;
	/**
	 *
	 */
	private String fellowPhotoId;
	private String fellowPhoto;

	/**
	 * 证件类型
	 */
	private Integer certType;

	private String certTypeDesc;


	/**
	 * 证件号码
	 */
	private String certNo;

	/**
	 * 证件图片
	 */
	private String certPic;

}
