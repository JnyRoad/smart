package com.tce.smart.platform.api.dto.resp.news;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author fushiping
 * @date 2022-02-16 18:00:02
 */
@Data
public class NewsDetailsRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@ApiModelProperty(value = "ID")
	private Long id;

	@ApiModelProperty(value = "消息标题")
	public String infoName;

	@ApiModelProperty(value = "消息类型Code")
	private Integer type;

	@ApiModelProperty(value = "发布状态code")
	private Integer status;

	@ApiModelProperty(value = "文本")
	private String content;

	@ApiModelProperty(value = "文本样式")
	private String textStyle;

	@ApiModelProperty(value = "文字移动方式")
	private Integer textMoveType;

	@ApiModelProperty(value = "文件")
	private NewsFileRespDTO file;

	@ApiModelProperty(value = "轮播图片")
	private List<NewsImageRespDTO> images;

}
