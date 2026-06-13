package com.tce.smart.app.ao;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;

@Data
public class AddAppSubjectAo {

	/**
	 * 主题ID
	 */
	private Integer id;
	/**
	 * 上级主题
	 */
//	private Integer parentSubject;
	/**
	 * 主题名称
	 */
	private String subjectName;
	/**
	 * 主题链接
	 */
	private String subjectUrl;

	/**
	 * 文本名称
	 */
	private String textName;
	/**
	 * 文本内容
	 */
	private String textDesc;
	/**
	 * 图片二进制
	 */
	private String picBinary;
	/**
	 * 附件
	 */
	private String enclosure;
	/**
	 * 附件名
	 */
	private String enclosureName;
	/**
	 * 园区id
	 */
	private Integer parkId;

}
