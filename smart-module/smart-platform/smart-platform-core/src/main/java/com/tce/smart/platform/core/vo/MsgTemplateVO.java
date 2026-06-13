package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 短信模板
 * @author fushiping
 * @date 2019/10/10 15:50
 **/
@Data
public class MsgTemplateVO extends BaseVO {

	private static final long serialVersionUID = 7670205472926789395L;
	/**
	 * 主键
	 */
	private String id;
	/**
	 * 模板名称
	 */
	private String tempName;

}
