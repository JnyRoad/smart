package com.tce.smart.platform.api.dto.req;

import com.tce.smart.platform.api.dto.SmtFellowVisitorDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加随行人员的添加数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveFellowVisitorReqDTO extends SmtFellowVisitorDTO {
private static final long serialVersionUID = 646649560362536078L;

   private String fellowPhoto;


	private String visitorPhone;

	/**
	 * 证件类型
	 */
	private Integer certType;

	/**
	 * 证件号码
	 */
	private String certNo;

	/**
	 * 证件图片
	 */
	private String certPic;
}
