package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 新员工须知详情
 *
 * @author mckaywu
 * @date 2019-06-05 11:16:06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NewEmployeeNoteDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 3498595113412125698L;

	/**
	 * 主键ID
	 */
	private String noteId;

	/**
	 * 须知名称
	 */
	private String noteName;

	/**
	 * 须知图片
	 */
	private String noteImage;

	/**
	 * 须知内容
	 */
	private String noteContent;

	/**
	 * 创建时间
	 */
	private LocalDateTime date;

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
