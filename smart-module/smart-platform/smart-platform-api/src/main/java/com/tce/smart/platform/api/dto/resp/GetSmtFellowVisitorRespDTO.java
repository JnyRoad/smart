package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
@Data
public class GetSmtFellowVisitorRespDTO extends BaseVO {
	private static final long serialVersionUID = -1021531921407673135L;


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
