package com.tce.smart.data.api.dto.temporary.req;

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
public class SaveEPhotoReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8635735330274470122L;

	/**
	 * 人事信息编号
	 */
	private Integer eid;

	/**
	 * 头像
	 */
	private String photo;

}
