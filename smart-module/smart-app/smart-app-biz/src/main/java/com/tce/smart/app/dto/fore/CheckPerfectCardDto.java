package com.tce.smart.app.dto.fore;

import lombok.Data;


/**
 * OCR识别提交检查Dto
 *
 * @author mkwu
 * @date 2019-10-10
 */
@Data
public class CheckPerfectCardDto {

	/**
	 * 信息采集编号
	 */
	private Integer perfectId;

	/**
	 * 身份证号
	 */
	private String identityCard;

	/**
	 * 姓名
	 */
	private String name;

}
