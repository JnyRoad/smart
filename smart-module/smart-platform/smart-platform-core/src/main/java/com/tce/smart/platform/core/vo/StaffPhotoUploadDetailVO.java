package com.tce.smart.platform.core.vo;

import lombok.Data;
/**
 * 员工头像上传详情
 * @author QIPEI
 *
 */
@Data
public class StaffPhotoUploadDetailVO {



	private String badge;

	private String name;

	private String createTime;

	private Integer status;

	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 园区
	 */
	private String parkName;


	private String createUser;

	private String facePicUrl;

}
