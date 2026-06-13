package com.tce.smart.app.vo.wechat;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author liangyuan
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PhotoVisitorVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 9031267213133057157L;

	/**
	 * 图片的id
	 */
	private String photoId;

}
