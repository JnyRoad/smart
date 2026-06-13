package com.tce.smart.temporary.core.dto;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 保存EHR员工图片
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveEPhotoDto extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -3402901730443266639L;

	/**
	 * 人事信息编号
	 */
	private Integer eid;

	/**
	 * 头像
	 */
	private String photo;

}
