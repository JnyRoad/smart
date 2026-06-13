package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * description: 广告 <br>
 * date: 2020/2/10 16:32 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class AdverVo extends BaseVO {

	/**
	 * 图片地址URL
	 */
	String imageUrl;

	/**
	 * 图片连接
	 */
	String imageLink;

	/**
	 * 链接类型
	 */
	Integer contentLinkType;
}
