package com.tce.smart.app.vo;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class AppSubjectVo extends BaseVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键ID
	 */
	private Integer id;
	/**
	 * 主题名称
	 */
	private String subjectName;
	/**
	 * 主题链接
	 */
	private String subjectUrl;
	/**
	 * 排序
	 */
	private Integer subjectOrder;
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
	 * 附件内容
	 */
	private String enclosure;
	/**
	 * 附件名
	 */
	private String enclosureName;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 修改时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 园区id
	 */
	private Integer parkId;
	/**
	 * 园区名
	 */
	private String parkName;

}
