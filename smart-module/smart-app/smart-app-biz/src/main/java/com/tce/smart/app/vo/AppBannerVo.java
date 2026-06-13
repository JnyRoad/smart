package com.tce.smart.app.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fushiping
 * @date 2019/5/22 14:21
 **/
@Data
public class AppBannerVo extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键ID
	 */
	private Integer id;
	/**
	 * 主题链接
	 */
	private String subjectUrl;
	/**
	 * 内部主题名
	 */
	private String textName;
	/**
	 * 图片二进制
	 */
	private String picBinary;
	/**
	 * 发布状态（0:待发布；1:已发布；2:已下线））
	 */
	private String publishFlag;
}
