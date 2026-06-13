package com.tce.smart.platform.api.dto.req;

import lombok.Builder;
import lombok.Data;

/**
 * 完善员工信息
 *
 * @author fushiping
 * @date
 */
@Data
@Builder
public class SyncStaffFaceImgDTO {

	/**
	 * 员工工号
	 */
	private String dhrBadge;

	/**
	 * dhrBase64字符串
	 */
//	private String dhrCertnoPic;

//	/**
//	 * 人脸照片imageCode
//	 */
//	private String smtffImgPic;

	/**
	 * 身份证号
	 */
	private String certno;
}
