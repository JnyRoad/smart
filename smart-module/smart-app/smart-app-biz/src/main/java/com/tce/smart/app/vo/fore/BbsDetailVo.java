package com.tce.smart.app.vo.fore;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/***
 * description: 集团公告详情Vo <br>
 * date: 2019/11/13 9:50 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
public class BbsDetailVo extends BaseVO {
	/**
	 * 主键ID
	 */
	private Integer bbsId;
	/**
	 * 主题名称
	 */
	private String bbsTitle;
	/**
	 * 主题文本内容
	 */
	private String bbsContent;
	/**
	 * 主题图片
	 */
	private String bbsImg;

	/**
	 * 附件名
	 */
	private String enclosureName;

	/**
	 * 附件下载URL
	 */
	private String enclosureUrl;

	/**
	 * PDF附件预览地址
	 */
	private String previewUrl;
}
