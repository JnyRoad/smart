package com.tce.smart.app.vo;

import com.tce.smart.common.core.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主题列表返回
 * @author fushiping
 * @date 2019/10/15 10:11
 **/
@Data
public class AppSubjectListVo extends BaseVO {

	private static final long serialVersionUID = 1L;
	/**
	 * 主键ID
	 */
	@ApiModelProperty("记录Id")
	private Integer id;
	/**
	 * 主题名称
	 */
	@ApiModelProperty("主题名称")
	private String subjectName;
	/**
	 * 排序
	 */
	private Integer subjectOrder;

	/**
	 * 图片二进制
	 */
	@ApiModelProperty("封面图片地址")
	private String picBinary;
	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	private LocalDateTime createTime;
	/**
	 * 修改时间
	 */
	@ApiModelProperty("最后更新时间")
	private LocalDateTime updateTime;
	/**
	 * 园区id
	 */
	private Integer parkId;
	/**
	 * 园区名
	 */
	@ApiModelProperty("园区名称")
	private String parkName;

	/**
	 * 内容类型
	 */
	@ApiModelProperty("内容类型: 1.链接 2.文本内容 3.PDF")
	private Integer type;

}
