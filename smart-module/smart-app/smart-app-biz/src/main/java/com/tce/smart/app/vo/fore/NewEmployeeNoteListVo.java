package com.tce.smart.app.vo.fore;

import java.time.LocalDateTime;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 新员工须知列表
 *
 * @author mckaywu
 * @date 2019-06-05 11:16:06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NewEmployeeNoteListVo extends BaseVO {

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
	 * 须知链接
	 */
	private String noteUrl;

	/**
	 * 须知图片
	 */
	private String noteImage;

	/**
	 * 创建时间
	 */
	private LocalDateTime date;

	/**
	 * 内容链接类型
	 */
	private Integer contentLinkType;

}
